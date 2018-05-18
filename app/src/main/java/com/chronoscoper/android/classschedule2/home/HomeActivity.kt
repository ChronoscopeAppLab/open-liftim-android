/*
 * Copyright 2017-2018 Chronoscope
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chronoscoper.android.classschedule2.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.LiftimApplication
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.archive.ArchiveFragment
import com.chronoscoper.android.classschedule2.functionrestriction.OnlyManagerActivity
import com.chronoscoper.android.classschedule2.functionrestriction.getFunctionRestriction
import com.chronoscoper.android.classschedule2.home.info.EditInfoActivity
import com.chronoscoper.android.classschedule2.home.timetable.EditTargetDialog
import com.chronoscoper.android.classschedule2.setting.SettingsActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.transition.FabTransformTransition
import com.chronoscoper.android.classschedule2.util.EventMessage
import com.chronoscoper.android.classschedule2.weekly.EditWeeklyActivity
import com.chronoscoper.android.classschedule2.weekly.WeeklyFragment
import com.plusassign.odd.OddView
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
        DrawerLayout.DrawerListener {
    companion object {
        private const val TAG = "HomeActivity"
        const val EVENT_OPEN_TIMETABLE_EDITOR = "OPEN_TIMETABLE_EDITOR"
    }

    private val drawer by bindView<DrawerLayout>(R.id.activity_home)
    private val drawerMenu by bindView<NavigationView>(R.id.drawer_menu)
    private val fab by bindView<FloatingActionButton>(R.id.add)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val eggOverlay by bindView<ViewGroup>(R.id.egg_overlay)

    private var fabAction: Runnable? = null

    private val editTimetableFabAction by lazy {
        Runnable {
            FabTransformTransition.configure(
                    ContextCompat.getColor(this@HomeActivity, R.color.colorAccent))
            val activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, fab, getString(R.string.t_fab))
            startActivity(Intent(this, EditTargetDialog::class.java),
                    activityOptions.toBundle())
        }
    }

    private val editInfoFabAction by lazy {
        Runnable {
            startActivity(Intent(this, EditInfoActivity::class.java))
        }
    }

    private val editWeeklyFabAction by lazy {
        Runnable {
            if (LiftimContext.isManager()) {
                startActivity(Intent(this, EditWeeklyActivity::class.java))
            } else {
                val activityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this, fab, getString(R.string.t_fab))
                        .toBundle()
                startActivity(Intent(this, OnlyManagerActivity::class.java),
                        activityOptions)
            }
        }
    }

    private var oddView: OddView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        EventBus.getDefault().register(this)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            // green character spawns 10%
            if (Math.random() < 0.1) {
                oddView = OddView(this)
                val dp = resources.displayMetrics.density
                val size = (250 * dp).toInt()
                oddView!!.layoutParams = FrameLayout.LayoutParams(size, size)
                eggOverlay.addView(oddView)
                oddView!!.visibility = View.INVISIBLE
                Handler().postDelayed({
                    oddView!!.visibility = View.VISIBLE
                    oddView!!.showFace()
                }, 2000)
            }
        }

        drawerMenu.setCheckedItem(R.id.drawer_timetable)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.open_drawer, R.string.close_drawer)
        drawer.addDrawerListener(toggle)
        drawer.addDrawerListener(this)
        drawerMenu.setNavigationItemSelectedListener(this)
        toggle.syncState()

        initLiftimCodePager()

        fabAction = editTimetableFabAction

        fab.setOnClickListener {
            fabAction?.run()
        }
        showSyncStatusIfNeeded()

        // Default status bar icon color is set to white because we want to change drawer icon color
        // when drawer is opened but SYSTEM_UI_FLAG_LIGHT_STATUS_BAR has no effect if default
        // status bar icon color is black.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private var prePauseFabShown: Boolean = true

    override fun onPause() {
        super.onPause()
        prePauseFabShown = fab.isShown
        fab.hide()
    }

    override fun onResume() {
        super.onResume()
        if (prePauseFabShown) {
            Handler().postDelayed({
                fab.show()
            }, 350)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun showSyncStatusIfNeeded() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val status = prefs.getInt(getString(R.string.p_last_sync_status), 0)
        val message: String? =
                when (status) {
                    -1 -> getString(R.string.no_connection)
                    400 -> getString(R.string.support_finished)
                    503 -> getString(R.string.server_maintenance)
                    in 500 until 600 -> getString(R.string.server_error)
                    else -> null
                }
        if (message != null) {
            Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG)
                    .apply {
                        setAction(android.R.string.ok, { this.dismiss() })
                        show()
                    }
        }
        prefs.edit().remove(getString(R.string.p_last_sync_status)).apply()
    }

    @Suppress("UNUSED")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventMessage) {
        if (event.type == EVENT_OPEN_TIMETABLE_EDITOR) {
            editTimetableFabAction.run()
        }
    }

    private var contentFragment: Fragment? = null

    private fun replaceFragment(fragment: Fragment) {
        when (fragment) {
            is HomeFragment -> {
                fab.show()
                toolbar.title = getString(R.string.app_name)
            }
            is WeeklyFragment -> {
                if (LiftimContext.isManager()
                        && getFunctionRestriction(this).editWeekly) {
                    fab.show()
                } else {
                    fab.hide()
                }
                toolbar.title = getString(R.string.weekly)
            }
            is ArchiveFragment -> {
                fab.hide()
                toolbar.title = getString(R.string.archive)
            }
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
        contentFragment = fragment
    }

    fun onHomePageChanged(page: Int) {
        when (page) {
            0 -> {
                fab.show()
                fabAction = editTimetableFabAction
                drawerMenu.setCheckedItem(R.id.drawer_timetable)
            }
            1 -> {
                if ((LiftimContext.isManager() && getFunctionRestriction(this).addInfo)
                        || getFunctionRestriction(this).addNote) {
                    fabAction = editInfoFabAction
                    fab.show()
                } else {
                    fab.hide()
                }
                drawerMenu.setCheckedItem(R.id.drawer_info)
            }
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(drawerMenu)) {
            drawer.closeDrawer(Gravity.START)
        } else {
            if (contentFragment !is HomeFragment) {
                drawerMenu.setCheckedItem(R.id.drawer_timetable)
                replaceFragment(HomeFragment())
                fabAction = editTimetableFabAction
            } else {
                val homeFragment = contentFragment as? HomeFragment ?: return
                if (homeFragment.page != 0) {
                    homeFragment.page = 0
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    private fun initLiftimCodePager() {
        val pager = drawerMenu.getHeaderView(0)
                .findViewById<ViewPager>(R.id.liftim_code_pager)
        val adapter = LiftimCodePagerAdapter(supportFragmentManager, this)
        pager.adapter = adapter
        pager.currentItem = adapter.initialPosition
        pager.offscreenPageLimit = 0
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) = Unit

            override fun onPageSelected(position: Int) {
                val liftimCode = (pager.adapter as LiftimCodePagerAdapter)
                        .getLiftimCodeInfo(position).liftimCode
                PreferenceManager.getDefaultSharedPreferences(this@HomeActivity)
                        .edit()
                        .putLong(getString(R.string.p_default_liftim_code), liftimCode)
                        .apply()
                (application as LiftimApplication).initEnvironment()
                replaceFragment(HomeFragment())
                drawer.closeDrawer(Gravity.START)
                drawerMenu.setCheckedItem(R.id.drawer_timetable)
            }
        })
        pager.setPageTransformer(false,
                { page, position ->
                    page.alpha = 1 - Math.abs(position)
                })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_timetable -> {
                if (contentFragment !is HomeFragment) {
                    replaceFragment(HomeFragment.obtain(0))
                } else {
                    val fragment = contentFragment as? HomeFragment ?: return true
                    fragment.page = 0
                }
                fabAction = editTimetableFabAction
            }
            R.id.drawer_info -> {
                if (contentFragment !is HomeFragment) {
                    replaceFragment(HomeFragment.obtain(1))
                } else {
                    val fragment = contentFragment as? HomeFragment ?: return true
                    fragment.page = 1
                }
                fabAction = editInfoFabAction
            }
            R.id.drawer_weekly -> {
                replaceFragment(WeeklyFragment())
                fabAction = editWeeklyFabAction
            }
            R.id.drawer_archive -> {
                replaceFragment(ArchiveFragment())
            }
            R.id.drawer_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        drawer.closeDrawer(Gravity.START)
        return true
    }

    // <editor-folding desc="DrawerListener">

    override fun onDrawerStateChanged(newState: Int) {
        // ignore
    }

    private var isStatusBarIconDark = true

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        Log.d(TAG, "Sliding drawer: $slideOffset")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (slideOffset > 0.5f && isStatusBarIconDark) {
                Log.d(TAG, "Lighten status bar icon")
                // We use "toolbar" because drawerView has been gone when drawer closed.
                toolbar.systemUiVisibility = 0
                isStatusBarIconDark = false
            } else if (slideOffset <= 0.5f && !isStatusBarIconDark) {
                Log.d(TAG, "Darken status bar icon")
                toolbar.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                isStatusBarIconDark = true
            }
        }
    }

    override fun onDrawerClosed(drawerView: View) {
        Log.d(TAG, "Drawer closed")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onDrawerOpened(drawerView: View) {
        Log.d(TAG, "Drawer opened")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.systemUiVisibility = 0
        }
    }

    // </editor-folding>
}
