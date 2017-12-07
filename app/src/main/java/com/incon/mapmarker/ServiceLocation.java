package com.incon.mapmarker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;


public class ServiceLocation extends Service {
    Boolean islocfind = true;
    private LocationManager locMan;
    private Boolean locationChanged;
    private Handler handler = new Handler();
    public static Location curLocation;
    public static boolean isService = false;
    static String locAddress, oldLocAddress;
    Double lastlat, lastlong;

    LocationListener gpsListener = new LocationListener()
    {
        public void onLocationChanged(Location location) {
            if (curLocation == null) {
                curLocation = location;
                locationChanged = true;
            } else if (curLocation.getLatitude() == location.getLatitude() && curLocation.getLongitude() == location.getLongitude())
            {
                locationChanged = false;
                return;
            } else
                locationChanged = true;
            if (locationChanged) {
                curLocation = location;

                if (ActivityCompat.checkSelfPermission(ServiceLocation.this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ServiceLocation.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locMan.removeUpdates(gpsListener);

             //   Toast.makeText(getBaseContext(), "location lat:" + location.getLatitude() + "location lang:" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                new getLocAddressDetails().execute();
            }
            //	curLocation = location;
            //	 new getLocAddressDetails().execute();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status == 0)// UnAvailable
            {
            } else if (status == 1)// Trying to Connect
            {
            } else if (status == 2) {// Available
            }
        }

    };

    @Override
    public void onCreate() {


        super.onCreate();

        curLocation = getBestLocation();

        if (curLocation == null) ;
        else {

        }

        isService = true;
    }

    final String TAG = "LocationService";

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onStart(Intent i, int startId) {
        handler.postDelayed(GpsFinder, 1);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(GpsFinder);
        handler = null;
        isService = false;
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_STICKY;
    }*/
    public Runnable GpsFinder = new Runnable() {
        public void run() {
            isService = true;
            Location tempLoc = getBestLocation();
            //time= getLocTime();
            if (tempLoc != null)
                curLocation = tempLoc;
            tempLoc = null;
            // register again to start after 1 seconds...
            SharedPreferences app_Preferences = getApplicationContext().getSharedPreferences("myPrefs",
                    getApplicationContext().MODE_PRIVATE);
            boolean val = app_Preferences.getBoolean("isserviceon", false);
            if (val)
                new getLocAddressDetails().execute();


        }
    };

    private Location getBestLocation() {

        try {
            Location gpslocation = null;
            Location networkLocation = null;

            if (locMan == null) {
                locMan = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            }
            try {
                if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return null;
                    }
                    gpslocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                }
                if(locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                {
                    networkLocation = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (IllegalArgumentException e) {
                //Log.e(ErrorCode.ILLEGALARGUMENTERROR, e.toString());
                Log.e("error", e.toString());
            }
            if(gpslocation==null && networkLocation==null)
                return null;

            if(gpslocation!=null && networkLocation!=null){
                if(gpslocation.getTime() < networkLocation.getTime()){
                    gpslocation = null;
                    return networkLocation;
                }else{
                    networkLocation = null;
                    return gpslocation;
                }
            }
            if (gpslocation == null) {
                return networkLocation;
            }
            if (networkLocation == null) {
                return gpslocation;
            }
        }catch(Exception e)
        {

        }
        return null;
    }

    private class getLocAddressDetails extends AsyncTask<String, Void, String>
    {
        protected void onPreExecute()
        {

        }
        @Override
        protected String doInBackground(String... urls) {
            // TODO Auto-generated method stub
            try
            {
                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses =null;
                try {
                    try
                    {
                        addresses = geocoder.getFromLocation(curLocation.getLatitude(), curLocation.getLongitude(), 1);
                    }catch(Exception e)
                    {

                    }

                    if(addresses != null) {
                        Address returnedAddress = addresses.get(0);
                        StringBuilder strReturnedAddress = new StringBuilder("");
                        for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                        }
                        locAddress=strReturnedAddress.toString();
                    }
                    else{
                        Toast.makeText(getBaseContext(),"location not found.", Toast.LENGTH_SHORT).show();
                        try
                        {
                            addresses = geocoder.getFromLocation(curLocation.getLatitude(), curLocation.getLongitude(), 1);
                        }catch(Exception e)
                        {

                        }
                        if(addresses != null) {
                            Address returnedAddress = addresses.get(0);
                            StringBuilder strReturnedAddress = new StringBuilder("");
                            for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                            }
                            locAddress=strReturnedAddress.toString();
                        }
                        else
                        {
                            locAddress=null;
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    locAddress=null;
                }
            }catch(Exception e)
            {
                locAddress=null;
            }
            return null;

        }

    }




}
