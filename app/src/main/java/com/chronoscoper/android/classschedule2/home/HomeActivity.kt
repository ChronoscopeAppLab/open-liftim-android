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
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.LiftimApplication
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.archive.ArchiveFragment
import com.chronoscoper.android.classschedule2.home.info.EditInfoActivity
import com.chronoscoper.android.classschedule2.home.timetable.EditTargetDialog
import com.chronoscoper.android.classschedule2.home.timetable.EditTimetableActivity
import com.chronoscoper.android.classschedule2.setting.SettingsActivity
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.transition.FabExpandTransition
import com.chronoscoper.android.classschedule2.transition.FabTransformTransition
import com.chronoscoper.android.classschedule2.util.showToast
import com.chronoscoper.android.classschedule2.weekly.EditWeeklyActivity
import com.chronoscoper.android.classschedule2.weekly.WeeklyFragment
import kotterknife.bindView

class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val drawer by bindView<DrawerLayout>(R.id.activity_home)
    private val drawerMenu by bindView<NavigationView>(R.id.drawer_menu)
    private val fab by bindView<FloatingActionButton>(R.id.add)

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
            FabExpandTransition.configure(
                    ContextCompat.getColor(this@HomeActivity, R.color.colorAccent))
            val activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, fab, getString(R.string.t_fab))
            startActivity(Intent(this, EditInfoActivity::class.java),
                    activityOptions.toBundle())
        }
    }

    private val editWeeklyFabAction by lazy {
        Runnable {
            FabExpandTransition.configure(
                    ContextCompat.getColor(this@HomeActivity, R.color.colorAccent))
            val activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, fab, getString(R.string.t_fab))
            startActivity(Intent(this, EditWeeklyActivity::class.java),
                    activityOptions.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        drawerMenu.setCheckedItem(R.id.drawer_timetable)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.open_drawer, R.string.close_drawer)
        drawer.addDrawerListener(toggle)
        drawerMenu.setNavigationItemSelectedListener(this)
        toggle.syncState()

        initLiftimCodePager()

        fabAction = editTimetableFabAction

        fab.setOnClickListener {
            fabAction?.run()
        }
        showLiftimCodeName()
    }

    private fun showLiftimCodeName() {
        if (!hasWindowFocus()) return
        Handler().post {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@HomeActivity)
            val defaultLiftimCode = sharedPrefs.getLong(getString(R.string.p_default_liftim_code), -1)
            if (defaultLiftimCode < 0) {
                showToast(this, getString(R.string.no_joined_class), Toast.LENGTH_SHORT)
            } else {
                val liftimCodeInfo = LiftimContext.getOrmaDatabase().selectFromLiftimCodeInfo()
                        .liftimCodeEq(defaultLiftimCode)
                        .firstOrNull() ?: return@post
                showToast(this, liftimCodeInfo.name, Toast.LENGTH_LONG)
            }
        }
    }

    private var contentFragment: Fragment? = null

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
        contentFragment = fragment
    }

    fun onHomePageChanged(page: Int) {
        when (page) {
            0 -> {
                drawerMenu.setCheckedItem(R.id.drawer_timetable)
                fabAction = editTimetableFabAction
            }
            1 -> {
                drawerMenu.setCheckedItem(R.id.drawer_info)
                fabAction = editInfoFabAction
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
            // TODO: Disabled temporarily:(
//            R.id.drawer_archive -> {
//                replaceFragment(ArchiveFragment())
//            }
            R.id.drawer_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        drawer.closeDrawer(Gravity.START)
        return true
    }
}
