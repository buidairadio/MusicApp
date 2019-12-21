package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.models.provider.LocalSongProvider
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import kotlinx.android.synthetic.main.fragment_firebase.*

private const val TAG = "SOUND_CLOUD_FRAGMENT"
class FirebaseFragment: Fragment()
{

    lateinit var mActivity:HomeScreenActivity
    var btnChooseFile:CardView? = null
//    var btnUpload:Button? = null
    var tvFileName:TextView? = null
    val PICK_AUDIO_REQUEST = 1
    var pickedFileUri:Uri?=null
    lateinit var mProgressBar:ProgressBar


    private var mStorageTask:StorageTask<UploadTask.TaskSnapshot>? = null

    lateinit var recyclerView:RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initView(inflater.inflate(R.layout.fragment_firebase, container, false))
    }

    override fun onResume() {
        super.onResume()
        if(!mActivity.isNetworkAvailable())
            Toast.makeText(mActivity,"Your device is currently offline",Toast.LENGTH_SHORT).show()
    }

    private fun initView(view : View): View {
        mActivity = (activity as HomeScreenActivity);

        recyclerView = view.findViewById(R.id.rv_firebase_fragment_online_song_list)
        val playAll = view.findViewById<CardView>(R.id.cv_firebase_play_all_song)

        Glide.with(activity as HomeScreenActivity).load(if(HomeScreenActivity.darkModeEnabled){
            R.drawable.ic_play_all_dark}else{
            R.drawable.ic_play_all}).into(
            view.findViewById(R.id.iv_firebase_play_all_song))

        playAll.setOnClickListener{
            //(activity as HomeScreenActivity).playAllSong(0)
            mActivity.playAllOnlineSong(0)
//            Toast.makeText(mActivity, "Not specified how to play all online song yet",Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mActivity.onlineSongAdapter



        btnChooseFile = view.findViewById(R.id.btn_firebase_choose_file)
//        btnUpload = view.findViewById(R.id.btn_firebase_upload)
        tvFileName = view.findViewById(R.id.tv_firebase_file_name)
        mProgressBar = view.findViewById(R.id.pb_firebase_upload_progress)

        tvFileName?.text = if(pickedFileUri==null){"Browse"}else{pickedFileUri.toString()}





        btnChooseFile?.setOnClickListener {
            if(mActivity.isNetworkAvailable()){
                if(pickedFileUri==null)
                    openFileChooser()
                else
                    uploadFile()
            }else{
                Toast.makeText(mActivity,"Please enable wifi network",Toast.LENGTH_SHORT).show()
            }
        }

        Log.d(TAG,"Oncreated")
        return view
    }

    private fun openFileChooser(){
        val intent = Intent()
        intent.setType("audio/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent,PICK_AUDIO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_AUDIO_REQUEST&& resultCode == RESULT_OK&&data!=null&&data.data!=null){
            pickedFileUri = data.data
            tvFileName?.text = pickedFileUri.toString()

            Glide.with(mActivity).load(R.drawable.ic_file_upload_black_24dp).into(iv_firebase_choose_file)
        }
    }

    private fun getFileExtension(uri: Uri):String{
        val cr =  mActivity.contentResolver
        val mime = MimeTypeMap.getSingleton()

        return mime.getExtensionFromMimeType(cr.getType(uri))
    }
    private fun uploadFile(){
        if(mStorageTask!=null&&mStorageTask!!.isInProgress){
            Toast.makeText(mActivity,"Upload is in progress",Toast.LENGTH_SHORT).show()
            return
        }
        if(pickedFileUri == null){
            Toast.makeText(mActivity,"No file selected",Toast.LENGTH_SHORT).show()
        }else{

            val fileReference = mActivity.mStorageRef.child(System.currentTimeMillis().toString()+"."+getFileExtension(pickedFileUri!!))
            Toast.makeText(mActivity,"Start uploading file",Toast.LENGTH_SHORT).show()
            mStorageTask = fileReference.putFile(pickedFileUri!!)
                .addOnSuccessListener {
                    val handler = Handler()
                    handler.postDelayed( {
                        mProgressBar.progress = 0
                    },500)
                    fileReference.downloadUrl.addOnSuccessListener {
                        //it: URL den ba hat
                        //val song = Song(0,0,tvFileName?.text.toString().trim(),"",it.toString(),null,0,0,0);
                        Toast.makeText(mActivity,"Upload successful to"+it.toString(),Toast.LENGTH_SHORT).show()
                        val song = LocalSongProvider.getSongInfoFromUri(mActivity,pickedFileUri!!)
                        if(song!=null){

                            val metaRetriever = MediaMetadataRetriever()
                            metaRetriever.setDataSource(mActivity,pickedFileUri)
                            song.duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()

                            song.path = it.toString()
                            val uploadID = mActivity.mDatabaseRef.push().getKey()
                            if(uploadID!=null) {
                                mActivity.mDatabaseRef.child(uploadID).setValue(song)
                                Toast.makeText(mActivity,"Saved data to realtime database "+uploadID,Toast.LENGTH_SHORT).show()

                                //reset button
                                Glide.with(mActivity).load(R.drawable.ic_cloud_upload_black_24dp).into(iv_firebase_choose_file)
                                tvFileName?.text = "Browse"
                                pickedFileUri = null
                                recyclerView.scrollToPosition(mActivity.onlineSongList.size-1)
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(mActivity,it.message,Toast.LENGTH_SHORT).show()
                }.addOnProgressListener {
                    val progress = (100.0* it.bytesTransferred/it.totalByteCount)
                    mProgressBar.progress = progress.toInt()
                }
        }
    }

    override fun onStart() {
        Log.d(TAG,"Onstart")
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG,"Onstop")
    }


}

