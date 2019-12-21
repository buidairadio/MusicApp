package com.uet.teamduan.musicplayer.screens.HomeScreenActivity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.v4.view.GravityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.uet.teamduan.musicplayer.R
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.app_bar_home_screen.*
import android.view.*
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SwitchCompat
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.uet.teamduan.musicplayer.services.MusicPlayerService
import kotlinx.android.synthetic.main.player_bar_home_screen.*
import com.uet.teamduan.musicplayer.models.DatabaseModel
import com.uet.teamduan.musicplayer.models.MetadataModel
import com.uet.teamduan.musicplayer.models.MusicLoadingModel
import com.uet.teamduan.musicplayer.models.data.Category
import com.uet.teamduan.musicplayer.models.data.Playlist
import com.uet.teamduan.musicplayer.models.data.Song
import com.uet.teamduan.musicplayer.services.notifications.MusicPlayerNotification
import com.uet.teamduan.musicplayer.services.notifications.MusicPlayerNotification.Companion.foregroundRunning
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment.AllSongsFragment
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment.PlaylistFragment
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenUtils.FlashScreenAnimation
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenUtils.OnSwipeTouchListener
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.PlayingQueueFragment.PlayingQueueFragment
import com.uet.teamduan.musicplayer.screens.adapters.*
import com.uet.teamduan.musicplayer.screens.dialogs.MoreOptionsDialog
import com.uet.teamduan.musicplayer.screens.dialogs.TimePickerDialogFragment
import com.uet.teamduan.musicplayer.utils.*
import com.uet.teamduan.musicplayer.visual_effects.AnimationEffects
import jp.wasabeef.blurry.Blurry

class HomeScreenActivity : AppCompatActivity()
//    , NavigationView.OnNavigationItemSelectedListener
    , MusicsLoadingContract.View
    , MusicPlayerService.UpdateViewCallback
    , StandardSongViewHolder.ItemClickListener<Pair<Int,Song>>
    , SeekBar.OnSeekBarChangeListener
    , DatabaseContract.View
    , MusicsPlayerMetadataContract.View
    , StandardSongViewHolder.ItemMoreOptionClickListener
    , MoreOptionsDialog.MoreOptionActionCallback
    , MoreOptionsDialog.MoreOptionActionCallback2
    , TimePickerDialogFragment.MyTimePickerListener
{

    private val TAG = "HOME_SCREEN_ACTIVITY"
    private val REQUEST_PERMISSION_CODE = 7
    val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

    companion object{
        var darkModeEnabled = false
        var queueSetChanged:Boolean = false
    }

    var databasePresenter = DatabasePresenter(this,DatabaseModel(this))
    private var presenter = MusicsLoadingPresenter(this, MusicLoadingModel(this))
    private var musicsPlayerMetadataPresenter = MusicsPlayerMetadataPresenter(this, MetadataModel(this))




    private var service:MusicPlayerService? =null
    private lateinit var bottomSheet:BottomSheetBehavior<FrameLayout>
    private var stopped:Boolean = false
    private var fragmentCount:Int = 0
    var aCopyOfCurrentSongIndexToCarryWithinThisClass:Int = 0



    //this songAdapter is not used in this activity, but in other fragment.
    //we put it here so that it will be created once. not twice...
    //and the listviews that are on other fragments want to display songs , just set their adapter to this
    //and booom!! it is there. :))))
    lateinit var adapter: SongRecyclerViewAdapter
    var songList = ArrayList<Song>()//the one and only songlist
    private var songMap = LinkedHashMap<Long,Int>()//the one and only songlist


    lateinit var onlineSongAdapter: SongRecyclerViewAdapter
    var onlineSongList = ArrayList<Song>()//the one and only songlist
    var onlineSongInQueue:Boolean = false

    private val albumList = ArrayList<Category>()
    lateinit var albumAdapter:AlbumRecyclerViewAdapter

    private val artistList = ArrayList<Category>()
    lateinit var artistAdapter:AlbumRecyclerViewAdapter

    lateinit var favouriteAdapter: FavouriteRecyclerViewAdapter
    private var favouriteList = ArrayList<Song>()
    private var favouriteMap = LinkedHashMap<Int ,Long>()
    private var favouriteMap2 = LinkedHashMap<Long ,Int>()
    private var favouriteListChanged:Boolean  = false

    lateinit var playlistAdapter: PlaylistRecyclerViewAdapter
    private var playlistList = ArrayList<Playlist>()
    private var playlistSongMapList = ArrayList<LinkedHashMap<Int, Long>>()

    //now we want to provide something for debugging purpose
    //we want to bind to the service on resume and unbind it on stop
    /// which is parallel with the same stuff in onCreate and onDestroy
    inner class ListToAddToQueue{
        var listType:String = ListTypes.LIST_TYPE_ALL_SONGS
        var index:Int = 0
    }
    var listToAddToQueue = ListToAddToQueue()
    var queueFragmentOpened:Boolean = false
    var queueAdapter:PlayingQueueAdapter? = null

    var playingQueueFragment:PlayingQueueFragment? = null



    var headerView:View? = null





    //system events
    //here we initialize everything once and for all
    override fun onCreate(savedInstanceState: Bundle?) {
        changeTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)


        initView()
        //why do we call this function in onResume?
        //because the activity may had not stopped??? -> that means we should make sure that
        //we unbind it to the service only when we are destroyed. sothat the next time this activity is triggered tot start
        //it will encounter the onCreate event. and that is where we should call this initServiceIfExist()
        initServiceIfExist()

        checkPermissions()
    }


    override fun onStart(){
        super.onStart()
        Log.d(TAG,"Started")
        initFirebase()
    }
    override fun onResume() {
        super.onResume()
        if(stopped)
            initServiceIfExist()
        Log.d(TAG,"resume")

    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG,"PAUSED")
    }
    override fun onStop() {
        //checking for binding already inside this function
        stopped = true
        unbindServiceIfExist()
        //showPosterView()

        Log.d(TAG,"Stopped")
        super.onStop()
    }
    override fun onDestroy() {
        //unbindServiceIfExist()
        Log.d(TAG,"Destroyed")
        if(MusicPlayerService.serviceStarted&&!foregroundRunning){
            MusicPlayerService.stopStartService(this)
        }
        super.onDestroy()
        mDatabaseRef.removeEventListener(mDBListener)
    }








    var mStorage = FirebaseStorage.getInstance()
    val mStorageRef= FirebaseStorage.getInstance().getReference("uploads")
    val mDatabaseRootRef = FirebaseDatabase.getInstance().getReference()
    val mDatabaseRef = mDatabaseRootRef.child("uploads")
    lateinit var mDBListener: ValueEventListener
    var mobileDataEnabled = false
    lateinit var wifiManager:WifiManager
    lateinit var connectivityManager:ConnectivityManager

    fun isNetworkAvailable():Boolean{
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//        val mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return wifi.isConnected
    }
    private fun initFirebase(){
        mDBListener = mDatabaseRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext,p0.message,Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                Toast.makeText(applicationContext,"Database synced",Toast.LENGTH_SHORT).show()
                onlineSongList.clear()
                for(p in p0.children){
                    val song = p.getValue(Song::class.java)
                    if(song!=null){
                        song.firebaseStorageKey = p.key!!
                        onlineSongList.add(song)
                    }
                }
                onlineSongAdapter.notifyDataSetChanged()

                //update the songList in service
                queueSetChanged = true
                listToAddToQueue.listType = ListTypes.LIST_TYPE_ONLINE_SONGS
                putSongListToQueue()
            }
        })

        //get the mobile data enable state
        connectivityManager = applicationContext.getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
        //check if network connection available
        Log.d(TAG,"Wifi "+if(isNetworkAvailable()){
            "enabled"}else{
//            Toast.makeText(applicationContext,"Your device is currently offline")
            "disabled"})
    }

    private fun checkPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val p = Utils.checkPermission(baseContext,PERMISSIONS)
            if(p!=null){
                requestPermissions(p, REQUEST_PERMISSION_CODE)
            }else{
                onPermissionGranted()
            }
        }else{
            onPermissionGranted()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_PERMISSION_CODE->{
                if(grantResults.size>0&&grantResults[0]==PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED){
                        Log.d(TAG,"Permission of reading storage granted")
                        onPermissionGranted()
                    }
                }else{
                    Log.d(TAG,"Permission not granted")
                    finish()
                }
            }
        }
    }
    private fun onPermissionGranted(){
        //after the view is ready we run this. be cause the presenter will call this view for displaying something
        //which require the content view to be not null
        presenter.loadSong(songList, songMap, albumList, artistList)
        musicsPlayerMetadataPresenter.getShuffleState()

        adapter = SongRecyclerViewAdapter(this, songList,this, this)
        albumAdapter = AlbumRecyclerViewAdapter(albumItemListener,albumList,ListTypes.LIST_TYPE_ALBUM_SONGS,this)
        artistAdapter = AlbumRecyclerViewAdapter(albumItemListener,artistList,ListTypes.LIST_TYPE_ARTIST_SONGS,this)


        onlineSongAdapter = SongRecyclerViewAdapter(object:StandardSongViewHolder.ItemClickListener<Pair<Int, Song>>{
                override fun onItemClick(item:Pair<Int, Song>){
//                    Toast.makeText(applicationContext,"Play the online song list",Toast.LENGTH_SHORT).show()
                    playAllOnlineSong(item.first)
                }
            },onlineSongList,
            object:StandardSongViewHolder.ItemMoreOptionClickListener {
                override fun onItemMoreOptionClick(song: Song,view: View,songIndex: Int,playlistIndex: Int,listType: String)
                {
                    if(service?.isOnline!!)
                        return

//                    Toast.makeText(applicationContext,"More option not specified",Toast.LENGTH_SHORT).show()
                    MoreOptionsDialog.showPopupWindow3(applicationContext,view,false,object:MoreOptionsDialog.MoreOptionActionCallback3{
                        override fun onDeleteOnlineSong(song: Song) {
                            val key = song.firebaseStorageKey
                            val ref= mStorage.getReferenceFromUrl(song.path)
                            Log.d(TAG,"delete song "+song.path)
//                            Toast.makeText(applicationContext,"Song deleted "+song.path,Toast.LENGTH_SHORT).show()
                            ref.delete().addOnSuccessListener {
                                mDatabaseRef.child(key).removeValue()
                                Toast.makeText(applicationContext,"Song deleted",Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(applicationContext,"Failed to delete song",Toast.LENGTH_SHORT).show()

                            }
                        }
                    },song)
                }
            },this)


        addHomeScreenFragment()
    }










    //user interact events
    private val clickListener = object:View.OnClickListener {
        override fun onClick(v: View?) {
            if(MusicPlayerService.serviceBound){
                if(v?.id==cv_player_bar_play.id||v?.id==bt_state.id) {

                    AnimationEffects.touchBoundEffect(cv_player_bar_play)
                    AnimationEffects.touchBoundEffect(bt_state)
                    if(service?.musicPlayer!!.isPlaying)
                        service?.pauseMusicPlayer()
                    else {
                        playMusicInService(service?.currentSongIndex!!)
                    }
                    Log.d(TAG, "button clicked")
                }else {
                    when(v?.id) {
                        bt_next.id -> {
                            nextSongInService()
                        }
                        cv_player_bar_next.id->{
                            nextSongInService()
                        }
                        bt_prev.id -> {
                            prevSongInService()
                            Log.d(TAG, "button clicked")
                        }
                        cv_love.id->{
                            if(service?.isOnline!!)
                                Toast.makeText(applicationContext,"Not support like for online song",Toast.LENGTH_SHORT).show()
                            else{
                                databasePresenter.likeOrUnlike(service?.getCurrentSong()!!)
                                AnimationEffects.touchBoundEffect(cv_love)
                            }
                        }
                    }
                }
            }else{
                if(v?.id==cv_player_bar_play.id||v?.id==bt_state.id) {
                    if(songList.size>0)
                    playAllSong(0)
                }
            }
            when(v?.id) {
                iv_hide_player_screen.id->{
                    bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                bottom_sheet_peek.id->{
                    bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
                }

                cv_player_screen_playing_queue.id->{
                    if(!queueFragmentOpened) {
                        openPlayingQueueFragment()
                        Log.d(TAG,"Touch to open new queue")
                    }else{
                        showPosterView()
                        showQueueView()
                        Log.d(TAG,"Touch to open existing queue")
                    }
                }

                cv_loop_state.id->{
                    musicsPlayerMetadataPresenter.changeLoopState()
                }
                cv_shuffle_state.id->{
                    musicsPlayerMetadataPresenter.changeShuffleState()
                }


                R.id.cv_sleep_timer->{
                    openTimePicker()
                }
                R.id.iv_drawer_logo->{
                    showWebPage()
                }
            }
        }
    }


    //events that occur during application running
//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        // Handle navigation view item clicks here.
//        when (item.itemId) {
////            R.id.nav_equalizer-> {
////
////            }
////            R.id.nav_sleep_timer-> {
////            }
////            R.id.nav_skin_theme-> {
////                openColorChooser()
////            }
////            R.id.nav_settings-> {
////
////            }
//        }
//        return true
//
//    }

    //more options
    override fun onItemMoreOptionClick(song: Song,view:View,songIndex:Int, playlistIndex:Int,listType: String) {
        MoreOptionsDialog.showPopupWindow1(this@HomeScreenActivity,view,song,songIndex,playlistIndex, this,false,listType)
    }
    override fun onAddToNext(song: Song) {
        if(MusicPlayerService.serviceBound){
            service?.songList!!.add(service?.currentSongIndex!!+1,song)
            queueAdapter?.notifyItemInserted(service?.currentSongIndex!!+1)
            queueSetChanged = true
            doSomethingOnQueueHavingNewSongs()
            Toast.makeText(this,"Song will be played next",Toast.LENGTH_SHORT).show()

        }
    }
    override fun onPushToQueue(song:Song){
        if(MusicPlayerService.serviceBound){
            service?.songList!!.add(song)
            queueAdapter?.notifyItemInserted(service?.songList!!.size-1)
            queueSetChanged = true
            doSomethingOnQueueHavingNewSongs()
            Toast.makeText(this,"Added song to Queue",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onShare(song:Song){
        val uri = Uri.parse(Environment.getExternalStorageDirectory().path+ song.path)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
        startActivity(Intent.createChooser(shareIntent,"Send song to"))
    }

    override fun onDelete(index:Int){
        if(service?.songList!!.size>0){
            if(index==service?.currentSongIndex)
                nextSongInService()
            service?.songList!!.removeAt(index)
            queueAdapter?.notifyItemRemoved(index)
            queueAdapter?.notifyItemRangeChanged(index,service?.songList!!.size)
            queueSetChanged = true
        }
    }

    override fun onDeleteSongInPlaylist(songIndex: Int, playlistIndex: Int) {
        val p = playlistList[playlistIndex]
        databasePresenter.deleteSongInPlaylist(p, p.songList[songIndex])
        p.songList.removeAt(songIndex)
        showSongListFragment?.adapter?.notifyItemRemoved(songIndex)

        queueSetChanged = true

        if(p.songList.size==0){
            onDelete2(playlistIndex,ListTypes.LIST_TYPE_PLAYLIST_SONGS,true)
        }
    }
    override fun onLikeFromOptionsDialog(song:Song){
        if(service?.isOnline!!)
            Toast.makeText(applicationContext,"Not support like for online song",Toast.LENGTH_SHORT).show()
        else {
            databasePresenter.likeOrUnlike(song)
        }
    }



    override fun onAddToNext2(index:Int, listType:String){
        addOrPush(index,listType,false)
        doSomethingOnQueueHavingNewSongs()
    }
    override fun onPushToQueue2(index:Int, listType:String){
        addOrPush(index,listType,true)
        doSomethingOnQueueHavingNewSongs()
    }
    override fun onRename2(index:Int, listType:String){
        PlaylistFragment.createDialogToRename(this,playlistList[index],index,playlistAdapter,layoutInflater)
    }

    override fun onDelete2(index:Int, listType:String,isOpenFromFragment:Boolean){
        when(listType){
            ListTypes.LIST_TYPE_PLAYLIST_SONGS->{
                Toast.makeText(this,"Deleted playlist "+playlistList[index].title,Toast.LENGTH_SHORT).show()
                databasePresenter.deletePlaylist(playlistList[index])
                playlistList.removeAt(index)
                playlistAdapter.notifyItemRemoved(index)
                queueSetChanged = true
                if(isOpenFromFragment){
                    popBackUptoHomeScreenFragment()
                }
             }
        }
    }

    override fun onPlayAll(index:Int, listType:String){
        listToAddToQueue.listType = listType
        listToAddToQueue.index = index
        playMusicInService(0)
    }


    private fun addOrPush(index:Int,listType:String,type:Boolean){
        when(listType){
            ListTypes.LIST_TYPE_PLAYLIST_SONGS->{
                addToNextInQueue(playlistList[index].songList,type)
            }
            ListTypes.LIST_TYPE_ALBUM_SONGS->{
                addToNextInQueue(albumList[index].songList,type)
            }
            ListTypes.LIST_TYPE_ARTIST_SONGS->{
                addToNextInQueue(artistList[index].songList,type)
            }
        }

    }
    private fun addToNextInQueue(list:ArrayList<Song>,end:Boolean = false){
        if(MusicPlayerService.serviceBound){
            if(end) {
                service?.songList!!.addAll(list)
            }
            else {
                service?.songList!!.addAll(service?.currentSongIndex!! + 1, list)
            }
            if(serviceSongQueueCallback!=null)
                serviceSongQueueCallback?.notifyQueueChanged()
            Toast.makeText(this,"Added "+list.size+" songs",Toast.LENGTH_SHORT).show()
            queueSetChanged = true
        }
    }

    interface ServiceSongQueueCallback{
        fun notifyQueueChanged()
    }
    var serviceSongQueueCallback:ServiceSongQueueCallback? = null



    private fun doSomethingOnQueueHavingNewSongs(){
        ll_player_bar.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).setListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                ll_player_bar.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500).setListener(object:AnimatorListenerAdapter(){
                    override fun onAnimationEnd(p0: Animator?) {
                        ll_player_bar.scaleX = 1.0f
                        ll_player_bar.scaleY = 1.0f
                        }
                    }).start()
            }
        }).start()
    }



    //end of seek bar listener//////////////////////////////////////////////////////////






    //click from songList
    override fun onItemClick(item: Pair<Int,Song>) {
        //here is the trigger point of the songIndexManager
        playAllSong(item.first)
        //bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    }
    fun playAllOnlineSong(fromIndex:Int){
        listToAddToQueue.listType = ListTypes.LIST_TYPE_ONLINE_SONGS
        playMusicInService(fromIndex)

        if(serviceSongQueueCallback!=null)
            serviceSongQueueCallback?.notifyQueueChanged()
    }

    fun playAllSong(fromIndex:Int){
        listToAddToQueue.listType = ListTypes.LIST_TYPE_ALL_SONGS
        playMusicInService(fromIndex)

        if(serviceSongQueueCallback!=null)
            serviceSongQueueCallback?.notifyQueueChanged()
    }
    //click from favourite list
    val favouriteItemListener = object:StandardSongViewHolder.ItemClickListener<Pair<Int,Song>>{
        override fun onItemClick(item: Pair<Int, Song>) {
            //here we come the other list
            listToAddToQueue.listType = ListTypes.LIST_TYPE_FAVOURITE_SONGS
            playMusicInService(item.first)
            //bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    fun onPlayAllButtonClickedInFavouriteSongFragment(){
        //here we playMusicPlayer the favourite list from 0
        listToAddToQueue.listType = ListTypes.LIST_TYPE_FAVOURITE_SONGS
        playMusicInService(0)
    }

    private val queueItemListener = object: PlayingQueueAdapter.QueueListener {
        override fun onItemClick(item: Int,fakeAdapter:Boolean) {
            if(fakeAdapter)
                playAllSong(item)
            else {
                listToAddToQueue.listType = ListTypes.LIST_TYPE_QUEUE_SONGS
                playMusicInService(item)
            }
        }
        override fun onDrag(view:View?, event:DragEvent?){

        }

        override fun onMoreButtonClick(view:View,index:Int) {

            MoreOptionsDialog.showPopupWindowQueueItem(this@HomeScreenActivity,view,index,this@HomeScreenActivity,false)
        }

        override fun onCurrentSongIndexUpdated(currentSongIndex: Int) {
            if(MusicPlayerService.serviceBound)
                service?.currentSongIndex = currentSongIndex
        }
    }
    val playlistSongItemListener = object:StandardSongViewHolder.ItemClickListener<Pair<String,Pair<Int, Int>>>{
        override fun onItemClick(item: Pair<String,Pair<Int,Int>>) {
            //here we come the other list
            listToAddToQueue.listType = item.first//ListTypes.LIST_TYPE_PLAYLIST_SONGS
            listToAddToQueue.index = item.second.second
            playMusicInService(item.second.first)
            //bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }



    private val playlistItemListener = object:PlaylistRecyclerViewAdapter.PlaylistListener{
        override fun onItemClick(item: Int,songList:ArrayList<Song>?, listType:String) {
            //open the playlist
            Log.d(TAG, "Open the playlist")
            openShowSongListFragment(item,playlistList[item].title,playlistList[item].songList,ListTypes.LIST_TYPE_PLAYLIST_SONGS)
        }
        override fun onPlaylistOptionClick(item: Int,view:View) {
            //do something with the playlist such as delete it
            Log.d(TAG, "Do something with the playlist")
            MoreOptionsDialog.showPopupWindow2(this@HomeScreenActivity,view,item,ListTypes.LIST_TYPE_PLAYLIST_SONGS,this@HomeScreenActivity,false,false)
        }
    }


    val albumItemListener = object: AlbumRecyclerViewAdapter.AlbumListener {
        override fun onItemClick(item: Int,list:ArrayList<Category>, listType:String) {
            //open the playlist
            Log.d(TAG, "Open the album or artist")
            openShowSongListFragment(item,list[item].title,list[item].songList,listType)
        }
        override fun onPlaylistOptionClick(item: Int,view:View,listType:String) {
            //do something with the playlist such as delete it
            Log.d(TAG, "Do something with the album or artist")
            MoreOptionsDialog.showPopupWindow2(this@HomeScreenActivity,view,item,listType,this@HomeScreenActivity,false,true)
        }
    }

    var showSongListFragment:ShowSongListFragment?=null
    private fun openShowSongListFragment(index:Int,title:String, list:ArrayList<Song>,listType:String){
        val bundle = Bundle()
        bundle.putParcelableArrayList("songList",list)
        bundle.putInt("playlistIndex",index)
        bundle.putString("listType",listType)
        bundle.putString("listTitle",title)

        showSongListFragment = ShowSongListFragment()
        showSongListFragment!!.arguments = bundle
        replaceFragment(showSongListFragment!!)

        supportActionBar?.setTitle(title)
    }


    private fun openPlayingQueueFragment(){
        if(!queueFragmentOpened) {
            playingQueueFragment = PlayingQueueFragment()
            replaceFragment(R.id.rl_queue_display,playingQueueFragment!!)
            serviceSongQueueCallback = playingQueueFragment
            queueFragmentOpened = true

            rl_player_screen_poster.animate().x(-rl_player_screen_poster.width.toFloat())
            Glide.with(applicationContext).load(R.drawable.ic_playlist_white_click).into(iv_queue_or_poster)
        }
    }


    fun doSomethingOnServiceBound(){
        queueAdapter = PlayingQueueAdapter(queueItemListener, service?.songList!!,this,service?.currentSongIndex!!,false)
        //replace the existing playing queue fragment
        if(queueFragmentOpened) {
            val temp = PlayingQueueFragment()
            supportFragmentManager.beginTransaction().remove(playingQueueFragment!!).replace(R.id.rl_queue_display,temp).commit()
            playingQueueFragment = temp
            serviceSongQueueCallback = playingQueueFragment
        }

        service?.waveformVisualizer?.waveformView = wv_player_screen
        service?.databasePresenter = databasePresenter
//        service?.musicPlayerNotification?.updateNotificationView(
//            songList[aCopyOfCurrentSongIndexToCarryWithinThisClass],
//            service?.musicPlayer?.isPlaying!!,false)
    }





    //events that occur during application at loading only

    //on loaded song list from MusicLoadingModel content resolver
    //we notify the data set adapter and we trigger the favourite song loading from SQLite to start
    override fun onLoadedSongList() {
        adapter.notifyDataSetChanged()
        databasePresenter.getFavouriteSongs(favouriteList, songList, songMap,favouriteMap)
        databasePresenter.getAllPlaylists(playlistList,playlistSongMapList,songList, songMap)

        for((i,song) in favouriteList.withIndex()) favouriteMap2.put(song.id,i)


        if(!MusicPlayerService.serviceRunning)
            queueAdapter = PlayingQueueAdapter(queueItemListener, songList,this, 0,true)

        if(!MusicPlayerService.serviceBound)
            updateViewOnNewSong(false,songList[0],0,0,false)


        if(songList.size>0) {
            tv_player_peek_title.text = songList[0].title
            tv_player_peek_artist.text = songList[0].artist
            tv_player_screen_title.text = songList[0].title
            tv_player_screen_artist.text = songList[0].artist
        }
        Log.d(TAG,"Loaded all songs with artists "+artistList.size)

    }

    override fun showErrorMessage() {
        Log.d(TAG,"Something went wrong with loading music")
    }

    override fun onLoadedThumbails() {
        Log.d(TAG,"Thumbail loaded")
        adapter.notifyDataSetChanged()
    }


    //override from service to update views
    override fun updateViewOnNewSong(state:Boolean,song: Song,newSongIndex:Int, oldSongIndex:Int, shuffleNext:Boolean) {
        super.updateViewOnNewSong(state,song,newSongIndex, oldSongIndex,shuffleNext)
        if(!MusicPlayerService.serviceBound)return

        tv_player_peek_title.text = song.title
        tv_player_peek_artist.text = song.artist
        tv_player_screen_title.text = song.title
        tv_player_screen_artist.text = song.artist

        iv_player_screen_overlay.visibility = View.INVISIBLE
        iv_player_screen_photo.visibility = View.INVISIBLE



        if(song.thumb!=null){
            iv_player_bar_thumb.setImageBitmap(song.thumb)

            iv_player_screen_overlay.visibility = View.VISIBLE
            iv_player_screen_photo.visibility = View.VISIBLE

            if(iv_player_screen_photo.width>0) {
                Blurry.with(this).radius(100).async()
                    .from(song.thumb!!.scaleAndCropCenter(Utils.DPToPX(song.thumb!!.width,this), Utils.DPToPX(song.thumb!!.height,this)))
                    .into(iv_player_screen_photo)
            }else{
                Blurry.with(this).radius(100).async()
                    .from(song.thumb!!.scaleAndCropCenter(Utils.DPToPX(song.thumb!!.width,this), Utils.DPToPX(song.thumb!!.height,this)))
                    .into(iv_player_screen_photo)
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                wv_player_screen.image = cropCircleBitmap(song.thumb!!)
            }else{
                wv_player_screen.image = song.thumb!!
            }
        }else{
            Glide.with(applicationContext).load(R.drawable.ic_songs).into(iv_player_bar_thumb)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wv_player_screen.image =
                    BitmapFactory.decodeResource(resources, R.drawable.ic_songs_large)
            }else{
                wv_player_screen.image =
                    BitmapFactory.decodeResource(resources, R.drawable.ic_songs_large)
            }
        }
        Glide.with(applicationContext).load(if(song.liked){R.drawable.ic_like_click}else{R.drawable.ic_like_white}).into(iv_love)


        notifyAdapterOnItemChanged(song,oldSongIndex, newSongIndex, shuffleNext)
    }


    private fun notifyAdapterOnItemChanged(song:Song,oldSongIndex: Int,newSongIndex: Int, shuffleNext:Boolean = false){
        if(shuffleNext){
            Log.d(TAG,"old song "+oldSongIndex+" new song "+newSongIndex)
            if(oldSongIndex<newSongIndex){
                queueAdapter?.notifyItemRangeChanged(oldSongIndex,newSongIndex-oldSongIndex+1)
            }else{
                queueAdapter?.notifyItemRangeChanged(newSongIndex,oldSongIndex-newSongIndex+1)
            }
        }else {
            queueAdapter?.notifyItemChanged(oldSongIndex)
            queueAdapter?.notifyItemChanged(newSongIndex)
        }
        queueAdapter?.currentSongIndex = newSongIndex

        if(MusicPlayerService.serviceBound){
            if(service?.isOnline!!){
                Log.d(TAG,"ONLINE")

                //online
                if(previousSongIndex != -1){

                    if(!onlineSongInQueue){
                        onlineSongInQueue = true
                        //from offline to online
                        Log.d(TAG,"FROM OFFLINE TO ONLINE")
                        if(previousSongIndex != -1 &&songList.size>0){
                            songList[previousSongIndex].isPlaying = false
                            adapter.notifyItemChanged(previousSongIndex)

                        }
                    }else{
                        if (newSongIndex != previousSongIndex) {
                            onlineSongList[previousSongIndex].isPlaying = false
                            onlineSongAdapter.notifyItemChanged(previousSongIndex)
                        }
                    }
                }else{
                    //bug
                    onlineSongInQueue = true
                }
                Log.d(TAG,"previousSongIndex "+previousSongIndex)


                onlineSongList[newSongIndex].isPlaying = true
                onlineSongAdapter.notifyItemChanged(newSongIndex)
                previousSongIndex = newSongIndex

            }else{
                Log.d(TAG,"OFFLINE")
                //offline
                val currentSongIndex = songMap[song.id]!!

                if(onlineSongInQueue) {
                    onlineSongInQueue = false
                    //from online to offline
                    Log.d(TAG,"FROM ONLINE TO OFFLINE")
                    if(previousSongIndex != -1 ){
                        onlineSongList[previousSongIndex].isPlaying = false
                        onlineSongAdapter.notifyItemChanged(previousSongIndex)
                    }
                }else if (previousSongIndex != -1 && currentSongIndex != previousSongIndex) {
                    songList[previousSongIndex].isPlaying = false
                    adapter.notifyItemChanged(previousSongIndex)
                }

                songList[currentSongIndex].isPlaying = true
                adapter.notifyItemChanged(currentSongIndex)
                favouriteAdapter.notifyDataSetChanged()
                previousSongIndex = currentSongIndex

                queueAdapter?.notifyItemChanged(service?.currentSongIndex!!)
            }
        }
    }
    var previousSongIndex  = -1

    override fun updateViewOnStateChange(state: Boolean,song:Song,index:Int) {
        if(!MusicPlayerService.serviceBound) return

        Glide.with(applicationContext).load(if(state){R.drawable.ic_pause_music_player}else{R.drawable.ic_play_music_player}).into(iv_state)
        Glide.with(applicationContext).load(if(state){R.drawable.ic_pause}else{R.drawable.ic_play_music}).into(iv_player_bar_state)

        if(MusicPlayerService.serviceBound) {
            if(service?.isOnline!!)
                onlineSongAdapter.notifyItemChanged(index)
            else{
                adapter.notifyItemChanged(songMap[song.id]!!)
                if(favouriteMap2.size>0&&song.liked)
                    favouriteAdapter.notifyItemChanged(favouriteMap2[song.id]!!)

                queueAdapter?.notifyItemChanged(service?.currentSongIndex!!)
            }
        }
    }


//    override fun updateOnLikeFromNotification(song: Song) {
//        if(!MusicPlayerService.serviceBound)return
//        if(song.liked)
//            updateOnLike(song)
//        else
//            updateOnUnlike(song)
//    }

    override fun onJumpFromOneEndToTheOther() {
        if(serviceSongQueueCallback!=null){
            serviceSongQueueCallback?.notifyQueueChanged()
        }
    }


    //favourite song implementation
    override fun updateOnLike(song:Song) {
        if(!MusicPlayerService.serviceBound)return
        Log.d(TAG,"updateOnLike")
        Glide.with(applicationContext).load(R.drawable.ic_like_click).into(iv_love)

        favouriteList.add(song)
        favouriteMap.put(favouriteList.size-1,song.id)
        favouriteMap2.put(song.id,favouriteList.size-1)
        favouriteAdapter.notifyItemInserted(favouriteList.size-1)

        favouriteListChanged = true
        service?.updateNotificationOnLike(song)

    }

    override fun updateOnUnlike(song:Song) {
        if(!MusicPlayerService.serviceBound)return
        Glide.with(applicationContext).load(R.drawable.ic_like_white).into(iv_love)
        for((index, fs) in favouriteList.withIndex()){
            if(fs.id == song.id){
                favouriteList.removeAt(index)
                favouriteMap.remove(index)
                favouriteMap2.remove(song.id)
                favouriteAdapter.notifyItemRemoved(index)
                favouriteListChanged = true
                break
            }
        }
        service?.updateNotificationOnLike(song)

    }

    override fun updateOnFavouritSongsLoaded(count: Int) {
        Log.d(TAG, "Loaded "+count+" favourite songs")
        favouriteAdapter.notifyDataSetChanged()
    }


    override fun updateOnPlaylistsLoaded(count: Int) {
        Log.d(TAG, "Loaded "+count+" playlists")
        playlistAdapter.notifyDataSetChanged()
    }

    override fun showMessageOnPlaylistCreated(playlistId: Long) {
        Log.d(TAG, "Created playlist "+playlistId)
        databasePresenter.getAllPlaylists(playlistList,playlistSongMapList,songList, songMap)
    }

    override fun showMessageOnPlaylistRenamed(playlistId: Long) {
        Log.d(TAG, "Renamed playlist "+playlistId)
        databasePresenter.getAllPlaylists(playlistList,playlistSongMapList,songList, songMap)
    }

    override fun showMessageOnDeletedSongFromPlaylist(playlist: Playlist, song: Song) {
        Log.d(TAG, "Delete song "+song.title+" from playlist "+playlist.title)
    }
    override fun updateOnShuffleStateChange(state: Boolean) {
        MusicPlayerService.shuffleState = state
        Glide.with(applicationContext).load(if(state){ R.drawable.ic_shuffle_click}else {R.drawable.ic_shuffle}).into(iv_shuffle_state)
        Log.d(TAG,"Shuffle state changed")
    }

    override fun updateOnLoopStateChange(state: Boolean) {
        MusicPlayerService.loopState = state
        Glide.with(applicationContext).load(if(!state){ R.drawable.ic_repeat}else {R.drawable.ic_repeat_one}).into(iv_loop_state)
        Log.d(TAG,"Loop state changed")
    }


    fun nextSongInService(){
        service?.nextSong()
        startServiceIfNotStartedYet()
    }

    fun prevSongInService(){
        service?.prevSong()
        startServiceIfNotStartedYet()
    }

    //call this function on:
    //1. first time clicked on the song
    //2. click on playMusicPlayer button on pauseMusicPlayer????
    //don't worry this function can only be called when the recyclerView was initialized. i.e songList was loaded
    fun playMusicInService(songIndex:Int){
        //how do we suppose to carry the songIndex to the boundEvent


        aCopyOfCurrentSongIndexToCarryWithinThisClass = songIndex

        startAndBindServiceIfNotRunning()

        if(MusicPlayerService.serviceRunning){
            //do we need to change the queue and which list you want to put to queue??
            putSongListToQueue()
            service?.playMusicPlayer(songIndex,true, false)
            //here we can check for if the notification has been dismissed or not
            startServiceIfNotStartedYet()
        }
    }

    private fun startAndBindServiceIfNotRunning(){
        if(!MusicPlayerService.serviceRunning){
            Log.d(TAG,"Not bound yet")
            val serviceIntent = Intent(this,MusicPlayerService::class.java)
            startService(serviceIntent)

            bindService(serviceIntent,serviceConnection,BIND_AUTO_CREATE)
        }
    }

    private fun startServiceIfNotStartedYet(){
        //here we can check for if the notification has been dismissed or not
        if(!MusicPlayerService.serviceStarted) {
            service?.initServiceObject(baseContext)
            startService(Intent(this, MusicPlayerService::class.java))
        }
    }

    //if the service is existing.. we don't need to load the songList from model again
    //just take a copy from the service's songList
    //and if the onServiceConnected arrived from here, dont initialize it again any more
    private fun initServiceIfExist(){
        //first instance is created here
        if(MusicPlayerService.serviceRunning){
            //bind to that service
            bindService(Intent(this,MusicPlayerService::class.java),serviceConnection,BIND_AUTO_CREATE)
        }
    }





    //ServiceConnection
    private val serviceConnection = object:ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG,"Service disconnected")
        }
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG,"Start passing data from activity to service")
            //now we ready to communicate and use the public functions of Service
            //at this point service must be null, always
            if(service == null){
                service = (binder as MusicPlayerService.MusicPlayerBinder).getServiceInstance()
                service?.callback = this@HomeScreenActivity
            }


            MusicPlayerService.serviceBound = true

            if(!MusicPlayerService.serviceRunning){
                //truyen songList cho sẻvice
                MusicPlayerService.serviceRunning = true
                playMusicOnBeginRunning()
            }else{
                //update view dua vao thong tin tu service
                aCopyOfCurrentSongIndexToCarryWithinThisClass=service?.currentSongIndex!!
                onlineSongInQueue = service?.isOnline!!
                if(!service?.isOnline!!)
                    updateViewOnNewSong(service?.musicPlayer!!.isPlaying, service?.getCurrentSong()!!,songMap[service?.getCurrentSong()!!.id]!!,aCopyOfCurrentSongIndexToCarryWithinThisClass,false)
            }


            if(intent.action== MusicPlayerNotification.INTENT_ACTION_FROM_NOTIFICATION){
                MusicPlayerService.openActivityFromNotification = true
                Log.d(TAG,"Open activity from notification")
            }
            doSomethingOnServiceBound()
        }
    }


    private fun playMusicOnBeginRunning(){
        //come from playMusicInService()
        //means we have to playMusicPlayer music for the first time
        service?.initServiceObject(baseContext)
        //which list you want to put to queue
        if(putSongListToQueue())
            service?.playMusicPlayer(aCopyOfCurrentSongIndexToCarryWithinThisClass,false, false)
    }

    fun putSongListToQueue():Boolean{
        //if new song list is the previous one?
        //if not then we add the list to the queue and save the pointer of that list
        if(
            (service?.queuePointer!=listToAddToQueue.listType
                    ||service?.queuePointer2!=listToAddToQueue.index
                    ||queueSetChanged
                    ||favouriteListChanged)
            &&listToAddToQueue.listType!=ListTypes.LIST_TYPE_QUEUE_SONGS){

            if(service?.queuePointer!=listToAddToQueue.listType)
               Log.d(TAG,"service?.queuePointer!=listToAddToQueue.listType")
            if(service?.queuePointer2!=listToAddToQueue.index)
                Log.d(TAG,"service?.queuePointer2!=listToAddToQueue.index")
            if(queueSetChanged)
                Log.d(TAG,"listToAddToQueue.queueSetChanged")

            val _songList = getListInQueue()
            if(_songList .size==0)return false
            service?.initSongQueue(_songList)
            service?.queuePointer = listToAddToQueue.listType
            service?.queuePointer2 = listToAddToQueue.index
            queueSetChanged = false
            favouriteListChanged = false
        }
        return true
    }




    private fun getListInQueue():ArrayList<Song>{
        return when(listToAddToQueue.listType){
            ListTypes.LIST_TYPE_ALL_SONGS->{
                if(service?.songList?.size!!>0&&!service?.isOnline!!){
                    notifyAdapterOnItemChanged(service?.getCurrentSong()!!,songMap[service?.getCurrentSong()!!.id]!!,listToAddToQueue.index)
                }
                service?.isOnline = false
                songList
            }
            ListTypes.LIST_TYPE_ONLINE_SONGS->{
                service?.isOnline = true
                onlineSongList
            }
            ListTypes.LIST_TYPE_FAVOURITE_SONGS->{
                if(service?.songList?.size!!>0&&!service?.isOnline!!){
                    adapter.notifyItemChanged(songMap[service?.getCurrentSong()!!.id]!!)
                    favouriteAdapter.notifyDataSetChanged()
                }
                service?.isOnline = false
                favouriteList
            }
            ListTypes.LIST_TYPE_PLAYLIST_SONGS->{
                service?.isOnline = false
                playlistList[listToAddToQueue.index].songList
            }
            ListTypes.LIST_TYPE_ALBUM_SONGS->{
                service?.isOnline = false
                albumList[listToAddToQueue.index].songList
            }
            ListTypes.LIST_TYPE_ARTIST_SONGS->{
                service?.isOnline = false
                artistList[listToAddToQueue.index].songList
            }else->{
                service?.isOnline = false
                songList
            }
        }
    }
    //we unbind the service on Stop
    private fun unbindServiceIfExist(){
        //if any song has been played since this activity started
        //that mean we had bound to the music player service
        //now we have to check if the singleton of that service say it was started
        if(MusicPlayerService.serviceRunning&&MusicPlayerService.serviceBound){
            Log.d(TAG,"Trying to disconnect with the service")
            if(!MusicPlayerService.openActivityFromNotification){
                unbindService(serviceConnection)
                MusicPlayerService.serviceBound = false
                if(songList.size>0)
                    service?.musicPlayerNotification?.updateNotificationView(songList[aCopyOfCurrentSongIndexToCarryWithinThisClass],service?.musicPlayer?.isPlaying!!,true)
                //null so that next time init you will create new service instance again
                service = null
                Log.d(TAG,"Unbound babe.......")
            }else{
                MusicPlayerService.openActivityFromNotification = false
            }
        }
    }



    private val bottomSheetCallback = object:BottomSheetBehavior.BottomSheetCallback(){
        override fun onSlide(p0: View, p1: Float) {
            Log.d(TAG,"BottomSheet: sliding up "+p1)
            ll_home_screen_fragment.visibility = View.VISIBLE
            ll_player_screen.visibility = View.VISIBLE

            abl_action_bar.y = -p1*abl_action_bar.height.toFloat()

            if(p1==0.0f) abl_action_bar.y = 0.0f

            bottom_sheet_peek.alpha = 1.0f-p1
            ll_player_screen_header.alpha = p1
        }

        override fun onStateChanged(p0: View, p1: Int) {
            when(p1){
                BottomSheetBehavior.STATE_HIDDEN->{
                    Log.d(TAG,"BottomSheet: state hidden")
                }
                BottomSheetBehavior.STATE_COLLAPSED->{
                    if(darkModeEnabled)
                        changeBackgroundOnBottomSheetCollapsed()
                    Log.d(TAG,"BottomSheet: state collapsed")
                    ll_player_screen.visibility = View.GONE

                    ll_home_screen_fragment.visibility = View.VISIBLE
                }
                BottomSheetBehavior.STATE_EXPANDED->{
                    Log.d(TAG,"BottomSheet: state expanded")
                    ll_home_screen_fragment.visibility = View.GONE
                    if(darkModeEnabled)
                        changeBackgroundOnBottomSheetExpanded()
                }
            }
        }
    }


    //this is seekbar listener//////////////////////////////////////////////////////////
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(MusicPlayerService.serviceBound&&fromUser)
            service?.setProgress(progress.toFloat()/sb_player_screen_seek_bar.max.toFloat())
    }
    override fun updateProgressBar(progress: Float, maxTime:Int) {
        val p = progress*sb_player_screen_seek_bar.max

        pb_player_screen_progress_bar.progress = p.toInt()
        sb_player_screen_seek_bar.progress = p.toInt()

        val currentTime = (progress*maxTime.toFloat()).toInt()//100 -> 40
        var m = currentTime/60000
        var s = (currentTime/1000)%60
        var time:String = ""+if(m<10){"0"+m}else{m}+":"+if(s<10){"0"+s}else{s}
        tv_time.text = time

        m = maxTime/60000
        s = (maxTime/1000)%60
        time = ""+if(m<10){"0"+m}else{m}+":"+if(s<10){"0"+s}else{s}
        tv_duration.text = time

    }






    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    //from fragments other than HomeScreenFragment, the call to this funtion is triggered
    //that means want to goback
    override fun onBackPressed(){
        if(bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        //these are fragments other than the HomeScreenFragment
        if(!popBackUptoHomeScreenFragment())
            super.onBackPressed()
    }






    //from fragment usage
    //from the HomeScreenFragment on the home OptionsItem is selected this function is called
    fun openDrawer(){
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }
    fun closeBottomSheet(){
        if(bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }


    fun replaceFragment(id:Int,fragment:Fragment){
        val transaction = supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.fragment_enter_right_to_left,R.anim.fragment_exit_right_to_left,
            R.anim.fragment_enter_left_to_right,R.anim.fragment_exit_left_to_right)
        transaction.replace(id,fragment,fragment.toString())
        transaction.commit()
    }




    fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.fragment_enter_right_to_left,R.anim.fragment_exit_right_to_left,
            R.anim.fragment_enter_left_to_right,R.anim.fragment_exit_left_to_right)
        transaction.replace(R.id.ll_home_screen_fragment,fragment,fragment.toString())
        transaction.addToBackStack(fragment.toString())
        transaction.commit()

        fragmentCount++

        if (fragmentCount>1){
            //these are fragments other than the HomeScreenFragment
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
            Log.d(TAG,"More than one fragment "+fragmentCount)
        }
    }

    fun popBackUptoHomeScreenFragment():Boolean{
        if(fragmentCount>1){
            supportFragmentManager.popBackStack()
            fragmentCount--
            Log.d(TAG,"pop fragment "+fragmentCount)
            if(fragmentCount==1){
                //this is homeScreenFragment
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
                supportActionBar!!.setTitle(R.string.title_activity_home_screen)
                Log.d(TAG,"Only one fragment left in the stack")
            }
            return true
        }
        return false
    }




    private fun initView(){
        headerView = nav_view.getHeaderView(0)
        changeBackground()
        FlashScreenAnimation.startFlashScreen(this)

        setSupportActionBar(toolbar)
//        nav_view.setNavigationItemSelectedListener(this)

        favouriteAdapter = FavouriteRecyclerViewAdapter(favouriteItemListener, favouriteList,this,this)
        playlistAdapter = PlaylistRecyclerViewAdapter(playlistItemListener,playlistList,this)

        //init song adapter

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)


        cv_player_bar_play.setOnClickListener(clickListener)
        bt_state.setOnClickListener(clickListener)
        bt_next.setOnClickListener(clickListener)
        bt_prev.setOnClickListener(clickListener)
        cv_loop_state.setOnClickListener(clickListener)
        cv_shuffle_state.setOnClickListener(clickListener)
        cv_love.setOnClickListener(clickListener)
        cv_player_screen_playing_queue.setOnClickListener(clickListener)
        cv_player_bar_next.setOnClickListener(clickListener)
        iv_hide_player_screen.setOnClickListener(clickListener)
        bottom_sheet_peek.setOnClickListener(clickListener)

        sb_player_screen_seek_bar.setOnSeekBarChangeListener(this)
        ll_player_screen.visibility = View.GONE
        tv_player_screen_title.isSelected = true
        tv_player_peek_title.isSelected = true




        headerView!!.findViewById<CardView>(R.id.cv_sleep_timer).setOnClickListener(clickListener)
        headerView!!.findViewById<ImageView>(R.id.iv_drawer_logo).setOnClickListener(clickListener)
        headerView!!.findViewById<LinearLayout>(R.id.ll_drawer_poster).background =
            ResourcesCompat.getDrawable(resources,R.drawable.bg_menu,null)

        val tbDarkMode = headerView!!.findViewById<SwitchCompat>(R.id.tb_dark_mode)
        tbDarkMode.isChecked = darkModeEnabled
        tbDarkMode.setOnCheckedChangeListener{ _ , b ->
            if (b) {
                Toast.makeText(this, "Enable", Toast.LENGTH_SHORT).show()
                applyColor(1)
            } else {
                Toast.makeText(this, "Disable", Toast.LENGTH_SHORT).show()
                applyColor(0)
            }
        }

        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        nav_view.layoutParams.width = (size.x.toFloat()*3.6f/5.0f).toInt()
        drawer_layout.addDrawerListener(object: ActionBarDrawerToggle(this, drawer_layout,R.string.open,R.string.close){
            override fun onDrawerSlide(drawerView:View, slideOffset:Float){
                super.onDrawerSlide(drawerView, slideOffset)
//                val slideX =drawerView.width*slideOffset
//                cl_home_screen.translationX = slideX/6*5
//                cl_home_screen.scaleX = 1.0f - slideOffset/4
//                cl_home_screen.scaleY = 1.0f - slideOffset/4
            }
        })

        bottomSheet = BottomSheetBehavior.from(fl_bottom_sheet)
        bottomSheet.setBottomSheetCallback(bottomSheetCallback)


        val swipeListener = object:OnSwipeTouchListener(this){
            override fun onSwipeRight() {
                if(queueFragmentOpened){
                    showPosterView()
                }
            }
            override fun onSwipeLeft() {
                if(!queueFragmentOpened) {
                    openPlayingQueueFragment()
                    Glide.with(applicationContext).load(R.drawable.ic_playlist_white_click).into(iv_queue_or_poster)
                }else {
                    showQueueView()
                }
            }
        }
        ll_player_screen.setOnTouchListener(swipeListener)

    }



    private fun openTimePicker(){
        val timePickerDialogFragment = TimePickerDialogFragment()

        val time = musicsPlayerMetadataPresenter.getPreviousTimer()

        timePickerDialogFragment.initTime(time/60,time%60)
        timePickerDialogFragment.show(supportFragmentManager,"time picker")
    }
    override fun setTime(hour: Int, minute: Int) {
        if(MusicPlayerService.serviceBound
            &&service?.musicPlayer!!.isPlaying) {

            service?.stopMusicHandler!!.removeCallbacks(service?.stopMusicRunnable)
            service?.stopMusicHandler!!.postDelayed(
                service?.stopMusicRunnable,
                hour.toLong() * 60 * 60 * 1000 + minute.toLong() * 60 * 1000
            )
            Toast.makeText(this,"Musics will stop in "+(minute+hour*60)+" minutes",Toast.LENGTH_SHORT).show()
        }else
            Toast.makeText(this,"Music not playing",Toast.LENGTH_SHORT).show()


        drawer_layout.closeDrawer(GravityCompat.START)
        musicsPlayerMetadataPresenter.saveTimer(hour*60+minute)
    }
    override fun onTimePickerCancel(){
        drawer_layout.closeDrawer(GravityCompat.START)
    }





    private fun applyColor(color: Int) {
        darkModeEnabled = (color==1)
        musicsPlayerMetadataPresenter.saveDarkmode(darkModeEnabled)
        finish()
        startActivity(intent)
        MusicPlayerService.openActivityFromNotification = true
    }


    private fun changeTheme(){
        darkModeEnabled = musicsPlayerMetadataPresenter.getDarkMode()
        if(darkModeEnabled){
            setTheme(R.style.AppThemeDark)
        }
        else {
            setTheme(R.style.AppTheme)
        }
    }
    private fun changeBackground(){
        setStatusBarGradiant(this)
        if(darkModeEnabled) {
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                ll_home_screen_fragment.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark))
            } else {
                ll_home_screen_fragment.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark))
            }
            changeToDarkIcons()
        }else{
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                ll_home_screen_fragment.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundLight))
            } else {
                ll_home_screen_fragment.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundLight))
            }
            changeToLightIcons()
        }
    }


    private fun addHomeScreenFragment(){
        supportFragmentManager.beginTransaction().add(R.id.ll_home_screen_fragment,AllSongsFragment(),"tag_home_screen_fragment").commit()
        fragmentCount = 1
    }

    private fun showWebPage(){
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://zergitas.com/")))
    }

    private fun showQueueView(){

        if (rl_player_screen_poster.x == 0.0f) {
            rl_player_screen_poster.animate().x(-rl_player_screen_poster.width.toFloat())
            rl_queue_display.animate().x(0.0f).setListener(object:Animator.AnimatorListener{
                override fun onAnimationEnd(p0: Animator?) {
                    (playingQueueFragment?.recyclerView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        aCopyOfCurrentSongIndexToCarryWithinThisClass, rl_queue_display.height/3)
                }
                override fun onAnimationRepeat(p0: Animator?) {
                }
                override fun onAnimationCancel(p0: Animator?) {
                }
                override fun onAnimationStart(p0: Animator?) {
                }

            })
            Glide.with(applicationContext).load(R.drawable.ic_playlist_white_click).into(iv_queue_or_poster)
            Log.d(TAG,"SHOw queue view")
        }
    }
    private fun showPosterView(){
        if(rl_player_screen_poster.x == -rl_player_screen_poster.width.toFloat()){
            rl_player_screen_poster.animate().x(0.0f)
            rl_queue_display.animate().x(rl_queue_display.width.toFloat())
            Glide.with(applicationContext).load(R.drawable.ic_playlist_white).into(iv_queue_or_poster)
            Log.d(TAG,"SHOw poster view")
        }
    }








    private fun changeToDarkIcons(){
        nav_view.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.colorBackgroundDark,null))

//        headerView!!.findViewById<RelativeLayout>(R.id.iv_footer).background =
//            ResourcesCompat.getDrawable(resources,R.drawable.bg_menu_dark,null)
        headerView?.findViewById<TextView>(R.id.tv_dark_mode)!!.setTextColor(ResourcesCompat.getColor(resources,R.color.colorBackgroundLight,null))
        headerView?.findViewById<TextView>(R.id.tv_sleep_timer)!!.setTextColor(ResourcesCompat.getColor(resources,R.color.colorBackgroundLight,null))


        fl_bottom_sheet.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.colorBackgroundDark,null))

    }
    private fun changeToLightIcons(){
        nav_view.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.colorBackgroundLight,null))
        headerView?.findViewById<TextView>(R.id.tv_dark_mode)!!.setTextColor(ResourcesCompat.getColor(resources,R.color.textColorDark,null))
        headerView?.findViewById<TextView>(R.id.tv_sleep_timer)!!.setTextColor(ResourcesCompat.getColor(resources,R.color.textColorDark,null))

        fl_bottom_sheet.background = ResourcesCompat.getDrawable(resources,R.drawable.gradient_left_right,null)

    }


    private fun changeBackgroundOnBottomSheetExpanded(){
        setStatusBarColor(this,R.color.colorBackgroundDark)
    }
    private fun changeBackgroundOnBottomSheetCollapsed(){
        setStatusBarGradiant(this)
    }

}
//                        toolbar.animate()
//                            .translationY(-toolbar.height.toFloat())
//                            .setListener(object : AnimatorListenerAdapter() {
//                                override fun onAnimationEnd(animation: Animator) {
//                                    super.onAnimationEnd(animation)
////                                    toolbar.visibility = View.GONE
//                                    toolbar.tag = "GONE"
//                                }
//                            })