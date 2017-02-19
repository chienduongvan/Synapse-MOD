/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.af.synapse;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import com.af.synapse.elements.*;
import com.af.synapse.lib.ActionValueNotifierHandler;
import com.af.synapse.lib.BootService;
import com.af.synapse.lib.ActionValueClient;
import com.af.synapse.lib.ActionValueUpdater;
import com.af.synapse.lib.ActivityListener;
import com.af.synapse.lib.ElementSelector;
import com.af.synapse.utils.ElementFailureException;
import com.af.synapse.utils.L;
import com.af.synapse.utils.NamedRunnable;
import com.af.synapse.utils.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Dialog dlg_restaurar = null;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    public static ViewPager mViewPager;

    public static TabSectionFragment[] fragments = null;
    private static AtomicInteger fragmentsDone = new AtomicInteger(0);
    long startTime;

    public static int topPadding = 0;
    public static int bottomPadding = 0;

    public static String BB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startTime = System.nanoTime();

        // Define Busybox
        if (new File("/su/xbin/busybox").exists())
            BB = "/su/xbin/busybox";
        else if (new File("/sbin/busybox").exists())
            BB = "/sbin/busybox";
        else BB = "/system/xbin/busybox";

        // Comprobar carpeta backup
        File bk_folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+getString(R.string.dir_backups));
        if (!bk_folder.exists()){
            bk_folder.mkdirs();
        }

        Utils.mainActivity = this;
        Utils.density = getResources().getDisplayMetrics().density;
        Utils.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Synapse.openExecutor();
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        Settings.setWallpaper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);

            if (Settings.getAppTheme() == Settings.Theme.TRANSLUCENT_DARK) {
                tintManager.setStatusBarTintEnabled(true);
                tintManager.setStatusBarTintResource(R.drawable.black_gradient_270);
            }

            if (Utils.hasSoftKeys(getWindowManager())) {
                tintManager.setNavigationBarTintEnabled(true);
                tintManager.setNavigationBarTintResource(R.drawable.black_gradient_90);
            }
        }

        getActionBar().hide();

        setPaddingDimensions();
        setContentView(R.layout.activity_loading);

        super.onCreate(fragments == null ? null : savedInstanceState);

        if (fragments == null) {
            if (Synapse.currentEnvironmentState != Synapse.environmentState.VALID_ENVIRONMENT) {
                findViewById(R.id.initialProgressBar).setVisibility(View.INVISIBLE);
                switch (Synapse.currentEnvironmentState) {
                    case ROOT_FAILURE:
                        ((TextView) findViewById(R.id.initialText)).setText(R.string.initial_no_root);
                        break;
                    case UCI_FAILURE:
                        ((TextView) findViewById(R.id.initialText)).setText(R.string.initial_no_uci);
                        break;
                    case JSON_FAILURE:
                        ((TextView) findViewById(R.id.initialText)).setText(R.string.initial_json_parse);
                }

                return;
            }
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        /**
         *  The UI building continues in buildFragment after fragment generation, or if
         *  the fragments are already live, continue here.
         */

        if (fragmentsDone.get() == Utils.configSections.size())
            continueCreate();
    }

    @SuppressWarnings("ConstantConditions")
    private void continueCreate() {
        View v = LayoutInflater.from(this).inflate(R.layout.activity_main, null);

        mViewPager = (ViewPager) v.findViewById(R.id.mainPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPagerPageChangeListener());
        mDrawerList = (ListView) v.findViewById(R.id.left_drawer);

        String[] section_titles = new String[Utils.configSections.size()];
        for (int i = 0; i < Utils.configSections.size(); i++)
            section_titles[i] = Utils.localise(((JSONObject)Utils.configSections.get(i)).get("name"));

        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, section_titles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemChecked(0, true);

        mDrawerLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                                                  R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle.syncState();

        // Comprobamos "aplicar_cambios_auto" si esta activo cancelamos para que deje entrar al nuevo perfil
        SharedPreferences prefs = getSharedPreferences("moro_prefs", Context.MODE_PRIVATE);
        int auto = prefs.getInt("aplicar_cambios_auto", 0);

        // Si es 1 cancelamos para que entre el perfil y reiniciamos
        if (auto == 1){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("aplicar_cambios_auto", 2);
            editor.commit();
            // cancelamos
            ActionValueUpdater.cancelElements();
            // reiniciamos
            Utils.runCommand("/res/synapse/uci restart", false);
        }
        // Si es 2 aplicamos para que coja los voltajes
        else if (auto == 2){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("aplicar_cambios_auto", 0);
            editor.commit();
            // aplicamos
            ActionValueUpdater.applyElements();
        }
        // Si es 0 no hacemos nada
        else if (auto == 0)
            ActionValueUpdater.refreshButtons(true);


        for (TabSectionFragment f : fragments)
            f.onElementsMainStart();

        setContentView(v);
        actionBar.show();
        Utils.appStarted = true;

        setPaddingDimensions();
        L.i("Interface creation finished in " + (System.nanoTime() - startTime) + "ns");

        if (!BootService.getBootFlag() && !BootService.getBootFlagPending()) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.popup_failed_boot_title)
                .setMessage(R.string.popup_failed_boot_message)
                .setCancelable(true)
                .setPositiveButton(R.string.popup_failed_boot_ack, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .show();
        }
    }

    private void setPaddingDimensions() {
        int resourceId;

        topPadding = 0;
        resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            topPadding += getResources().getDimensionPixelSize(resourceId);

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            topPadding += TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        View v;

        v = findViewById(R.id.activity_loading);
        if (v != null)
            v.setPadding(0, topPadding, 0, 0);

        v = findViewById(R.id.activity_main);
        if (v != null)
            v.setPadding(0, topPadding, 0, 0);

        if (Utils.appStarted && Utils.hasSoftKeys(getWindowManager())) {
            bottomPadding = 0;

            resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0)
                bottomPadding += getResources().getDimensionPixelSize(resourceId);


            if (bottomPadding == 0)
                return;

            for (TabSectionFragment f : fragments) {
                if (f != null && f.fragmentView != null)
                    f.fragmentView
                            .findViewById(R.id.section_container_linear_layout)
                            .setPadding(0, 0, 0, bottomPadding);
            }
        }
    }

    // Dialogo Restaurar perfil
    private void dialogo_restaurar(){

        File ficheros = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+getString(R.string.dir_backups));
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String ruta;
                if(pathname.isFile()){
                    ruta = pathname.getAbsolutePath().toLowerCase();
                    //compruebo que el nombre completo, con ruta, del archivo tiene la extensión que yo uso en la apk para backups
                    if(ruta.contains("."+getString(R.string.ext_backups))){
                        return true;
                    }
                }
                return false;
            }
        };
        File fa[]=ficheros.listFiles(ff);
        if (fa.length==0){
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.no_backups), Toast.LENGTH_LONG);
            toast.show();
        }
        else{

            AdapterBackups ab = new AdapterBackups();
            ab.AdapterBackups(this,fa);
            ListView lista = new ListView(this);
            lista.setAdapter(ab);
            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    String archivo = tv.getText().toString();
                    dlg_restaurar.dismiss();
                    dlg_restaurar=null;
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+
                            getString(R.string.dir_backups)+archivo+"."+getString(R.string.ext_backups));
                    //compruebo que físicamente existe de verdad (otra vez)
                    if(f.exists()) confirmar_restaurar(archivo);
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.no_backups), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
            AlertDialog.Builder abd = new AlertDialog.Builder(this);
            abd.setTitle(R.string.dlg_restore_title);
            abd.setMessage(R.string.dlg_restore_message);
            abd.setIcon(R.drawable.ic_action_restore);
            abd.setView(lista);
            dlg_restaurar = abd.create();
            dlg_restaurar.show();
        }

    }

    //Dialogo confirmar restauracion perfil
    private void confirmar_restaurar(final String backup){

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.dlg_conf_title);
        adb.setIcon(R.drawable.ic_alert);
        adb.setMessage(getString(R.string.dlg_conf_message, backup));
        adb.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Guardo boolean true para el cambio automatico del perfil
                SharedPreferences prefs = getSharedPreferences("moro_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("aplicar_cambios_auto", 1);
                editor.commit();

                 // Restauramos perfil
                Utils.runCommand(BB+" mount -o remount,rw /", false);
                Utils.runCommand("/res/synapse/actions/sqlite ImportConfigSynapse "+backup, false);
                Utils.runCommand("/res/synapse/uci restart", false);
                Utils.runCommand(BB+" mount -o remount,ro /", false);
            }
        });
        adb.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.create().show();
    }

    // Cuadro dialogo de guardar perfil
    protected void guardar_perfil() {

        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(R.string.dlg_backup_title);
        adb.setIcon(R.drawable.ic_action_save);
        adb.setMessage(R.string.dlg_backup_message);

        final EditText editText = new EditText(MainActivity.this);
        editText.setHint(R.string.dlg_backup_box);
        adb.setView(editText);

        // Si pulsamos OK, cargamos el dialogo de confirmacion de carga de perfil
        adb.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Guardamos el perfil
                String name = editText.getText().toString().replace(' ','_');
                Utils.runCommand(BB+" mount -o remount,rw /", false);
                Utils.runCommand("/res/synapse/actions/sqlite ExportConfigSynapse "+name, false);
                Utils.runCommand(BB+" mount -o remount,ro /", false);

                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.dlg_bk_profile_toast)+" "+name, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        // Si pulsamos Cancelar salimos
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // create an alert dialog
        AlertDialog alert = adb.create();
        alert.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setPaddingDimensions();
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy(){
        if (!isChangingConfigurations()) {
            fragments = null;
            fragmentsDone = new AtomicInteger(0);
            Utils.appStarted = false;

            Settings.wallpaper = null;
            ActionValueNotifierHandler.clear();
            ActionValueUpdater.clear();
        }
        super.onDestroy();
        Utils.mainActivity = null;
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        ActionValueUpdater.setMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item))
            return true;

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_apply:
                ActionValueUpdater.applyElements();
                return true;
            case R.id.action_cancel:
                ActionValueUpdater.cancelElements();
                return true;
            case R.id.action_select_multi:
                startActionMode(ElementSelector.callback);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, Settings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                this.startActivity(intent);
                return true;
            case R.id.menu_backup:
                guardar_perfil();
                return true;
            case R.id.menu_restore:
                dialogo_restaurar();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            mViewPager.setCurrentItem(position, true);
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    private class ViewPagerPageChangeListener implements ViewPager.OnPageChangeListener {
        int previousPosition = 0;

        @Override
        public void onPageScrolled(int i, float v, int i2) {}

        @Override
        public void onPageSelected(int i) {
            mDrawerList.setItemChecked(i, true);
            fragments[i].onElementsResume();

            if (previousPosition != i)
                fragments[previousPosition].onElementsPause();

            previousPosition = i;
        }

        @Override
        public void onPageScrollStateChanged(int i) {}
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment
     * corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private void buildFragment(int position) {
            if (fragments[position] != null)
                return;

            TabSectionFragment fragment = new TabSectionFragment();
            Bundle args = new Bundle();
            args.putInt(TabSectionFragment.ARG_SECTION_NUMBER, position);
            fragment.setArguments(args);
            fragments[position] = fragment;
            fragment.prepareView();
            fragmentsDone.incrementAndGet();

            if (fragmentsDone.get() < Utils.configSections.size())
                return;

            /**
             *  After all fragments are created, continue building the UI.
             */
            Synapse.handler.post(new Runnable() {
                @Override
                public void run() {
                    continueCreate();
                }
            });
        }

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            if (fragmentsDone.get() > 0)
                return;

            if (fragments == null)
                fragments = new TabSectionFragment[Utils.configSections.size()];

            for (int i = 0; i < Utils.configSections.size(); i++) {
                /**
                 *  Spawn a builder thread for each section/fragment
                 */
                final int position = i;

                Synapse.executor.execute(new NamedRunnable(
                    new Runnable() {
                        @Override
                        public void run() { buildFragment(position); }
                    })
                {
                    @Override
                    public String getName() {
                        return Utils.localise(((JSONObject)Utils.configSections
                                                                .get(position))
                                                                .get("name"));
                    }
                });
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return Utils.configSections.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragments[position].onDetach();
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            JSONObject section = (JSONObject)Utils.configSections.get(position);
            return Utils.localise(section.get("name"));
        }
    }

    public static class TabSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */

        public static final String ARG_SECTION_NUMBER = "section_number";
        private int sectionNumber;

        public View fragmentView = null;
        public ArrayList<BaseElement> fragmentElements = new ArrayList<>();

        public TabSectionFragment() {
            this.setRetainInstance(true);
        }

        public void prepareView() {
            if (fragmentView != null)
                return;

            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            ScrollView tabSectionView = (ScrollView)LayoutInflater.from(Utils.mainActivity)
                                                        .inflate(R.layout.section_container, null);
            assert tabSectionView != null;
            LinearLayout tabContentLayout = (LinearLayout) tabSectionView.getChildAt(0);
            assert tabContentLayout != null;

            JSONObject section = (JSONObject)Utils.configSections.get(sectionNumber);
            JSONArray sectionElements = (JSONArray)section.get("elements");

            for (Object sectionElement : sectionElements) {
                JSONObject elm = (JSONObject) sectionElement;
                String type = Utils.getEnclosure(elm);
                JSONObject parameters = (JSONObject) elm.get(type);

                BaseElement elementObj;

                try {
                    elementObj = BaseElement.createObject(type, parameters, tabContentLayout, this);
                } catch (ElementFailureException e) {
                    tabContentLayout.addView(Utils.createElementErrorView(e));
                    continue;
                }

                if (elementObj instanceof ActionValueClient)
                    ActionValueUpdater.registerPerpetual((ActionValueClient) elementObj, sectionNumber);

                /**
                 *  Simple standalone elements may not add themselves to the layout, if so, add
                 *  them here after their creation.
                 */

                View elementView;

                try {
                    elementView = elementObj.getView();
                } catch (ElementFailureException e) {
                    View errorView = Utils.createElementErrorView(e);
                    if (errorView != null)
                        tabContentLayout.addView(errorView);
                    continue;
                }

                if (elementView != null)
                    tabContentLayout.addView(elementView);

                fragmentElements.add(elementObj);
            }

            fragmentView = tabSectionView;
        }

        //public boolean containsElement(BaseElement element) {
        //    return fragmentElements.contains(element);
        //}

        public void addElement(BaseElement element) {
            fragmentElements.add(element);
        }

        public void removeElement(BaseElement element) {
            fragmentElements.remove(element);
        }

        public int getSectionNumber() { return sectionNumber; }

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            prepareView();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            prepareView();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return fragmentView;
        }


        public void onElementsMainStart() {
            for (BaseElement elm : fragmentElements)
                if (elm instanceof ActivityListener)
                    try { ((ActivityListener) elm).onMainStart(); }
                    catch (ElementFailureException e) { Utils.createElementErrorView(e); }
        }

        @Override
        public void onStart(){
            super.onStart();
            onElementsStart();
        }

        public void onElementsStart() {
            for (BaseElement elm : fragmentElements)
                if (elm instanceof ActivityListener)
                    try { ((ActivityListener) elm).onStart(); }
                    catch (ElementFailureException e) { Utils.createElementErrorView(e); }
        }

        @Override
        public void onResume(){
            super.onResume();
            if (mViewPager.getCurrentItem() == sectionNumber)
                onElementsResume();
        }

        public void onElementsResume() {
            for (BaseElement elm : fragmentElements)
                if (elm instanceof ActivityListener)
                    try { ((ActivityListener) elm).onResume(); }
                    catch (ElementFailureException e) { Utils.createElementErrorView(e); }
        }

        @Override
        public void onPause(){
            super.onPause();
            if (mViewPager.getCurrentItem() == sectionNumber)
                onElementsPause();
        }

        public void onElementsPause(){
            for (BaseElement elm : fragmentElements)
                if (elm instanceof ActivityListener)
                    try { ((ActivityListener) elm).onPause(); }
                    catch (ElementFailureException e) { Utils.createElementErrorView(e); }
        }

        @Override
        public void onDetach(){
            /**
             *  On main activity destruction we are keeping the fragments instead of killing them.
             *  However on the next activity re-creation they need to get added to that new View
             *  instance. So we remove the child view from the old instance so that it can be
             *  added to the new one.
             */

            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup)fragmentView.getParent();
                if (parent != null)
                    parent.removeView(fragmentView);
            }
            super.onDetach();
        }
    }

    @Override
    public void onBackPressed() {
        if(Utils.appStarted) finish();
    }

    @Override
    public void onStop(){
        super.onStop();
        System.gc();
    }

    @Override
    public void onPause(){
        super.onPause();
        System.gc();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (Utils.appStarted)
            for (TabSectionFragment f : fragments)
                f.onElementsMainStart();

        System.gc();
    }
}
