package com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.view.*
import com.uet.teamduan.musicplayer.R
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.AllSongsScreenFragment.adapters.ViewPagerAdapter
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.HomeScreenActivity
import com.uet.teamduan.musicplayer.screens.HomeScreenActivity.SearchFragment


private const val TAG = "ALL_SONG_FRAGMENT"
class AllSongsFragment : Fragment()
{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initView(inflater.inflate(R.layout.fragment_all_songs, container, false),savedInstanceState)
    }

    private fun initView(v:View, savedInstanceState: Bundle?):View{
        val tab_layout = v.findViewById<TabLayout>(R.id.tab_layout_all_songs)
        val view_pager= v.findViewById<ViewPager>(R.id.view_pager_all_songs)

        tab_layout.addTab(tab_layout.newTab().setText("Songs"))
        tab_layout.addTab(tab_layout.newTab().setText("Firebase"))
        tab_layout.addTab(tab_layout.newTab().setText("Favorite"))
        tab_layout.addTab(tab_layout.newTab().setText("Albums"))
        tab_layout.addTab(tab_layout.newTab().setText("Playlist"))
        tab_layout.addTab(tab_layout.newTab().setText("Artists"))

        if(HomeScreenActivity.darkModeEnabled)
            tab_layout.setTabTextColors(
                ResourcesCompat.getColor(resources,R.color.colorBackgroundLight,null),
                ResourcesCompat.getColor(resources,R.color.textColorOrange,null))
        else
            tab_layout.setTabTextColors(
                ResourcesCompat.getColor(resources,R.color.textColorDark,null),
                ResourcesCompat.getColor(resources,R.color.textColorOrange,null))

        tab_layout.tabGravity = TabLayout.GRAVITY_FILL
        view_pager.adapter = ViewPagerAdapter(childFragmentManager, tab_layout.tabCount)
        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))

        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabReselected(p0: TabLayout.Tab?) {}
            override fun onTabSelected(p0: TabLayout.Tab?) {
                view_pager.currentItem = p0!!.position
            }
        })

        setHasOptionsMenu(true)
        return v
    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater): Unit {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_screen, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                (activity as HomeScreenActivity).openDrawer()
            }
            R.id.action_search->{
                (activity as HomeScreenActivity).replaceFragment(SearchFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }





}
