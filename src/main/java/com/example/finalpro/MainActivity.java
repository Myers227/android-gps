package com.example.finalpro;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // The map itself. We can add markers to it.
    private GoogleMap myMap;
    // Used for getting locations and places.
    private FusedLocationProviderClient fusedLocationClient;
    // Tracks user's location
    private LocationCallback locationCallback;
    // Gets the details on places.
    private PlacesClient placesClient;
    // Needed for pagination.
    private String nextPageToken = null;
    // The line drawn between two places.
    private Polyline routeLine;
    // The layout for the details when clicking on a place.
    private LinearLayout placeDetailsScreen;
    // The overlay to close the placeDetailsScreen.
    private View overlayView;
    // The screen that is shown if permissions are not granted.
    private View permissionView;
    // The layout for the main menu.
    private LinearLayout mainMenu;
    // The layout for the map.
    private FrameLayout mapScreen;
    // The layout for the favorites menu.
    private TableLayout favoritesLayout;
    // Keeps track of favorite places.
    private final List<Place> favoriteList = new ArrayList<>();
    // Turns the favoriteList into another list that can be saved using GSON.
    private final List<String> favoriteIDList = new ArrayList<>();
    // The button that favorites places.
    private Button favoriteButton;
    // The bar that shows a places rating.
    private RatingBar ratingBar;
    // Needed for AutoCompleteTextView.
    private final String[] placeTypes = {
            "accounting", "airport", "amusement_park", "aquarium", "art_gallery", "atm", "bakery",
            "bank", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley",
            "bus_station", "cafe", "campground", "car_dealer", "car_rental", "car_repair",
            "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store",
            "convenience_store", "courthouse", "dentist", "department_store", "doctor", "drugstore",
            "electrician", "electronics_store", "embassy", "fire_station", "florist",
            "funeral_home", "furniture_store", "gas_station", "gym", "hair_care", "hardware_store",
            "hindu_temple", "home_goods_store", "hospital", "insurance_agency", "jewelry_store",
            "laundry", "lawyer", "library", "light_rail_station", "liquor_store",
            "local_government_office", "locksmith", "lodging", "meal_delivery", "meal_takeaway",
            "mosque", "movie_rental", "movie_theater", "moving_company", "museum", "night_club",
            "painter", "park", "parking", "pet_store", "pharmacy", "physiotherapist", "plumber",
            "police", "post_office", "primary_school", "real_estate_agency", "restaurant",
            "roofing_contractor", "rv_park", "school", "secondary_school", "shoe_store",
            "shopping_mall", "spa", "stadium", "storage", "store", "subway_station",
            "supermarket", "synagogue", "taxi_stand", "tourist_attraction", "train_station",
            "transit_station", "travel_agency", "university", "veterinary_care", "zoo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        favoriteButton = findViewById(R.id.favorite_button);
        ratingBar = findViewById(R.id.ratingBar);

        mapScreen = findViewById(R.id.map_screen);
        mapScreen.setVisibility(View.GONE);

        placeDetailsScreen = findViewById(R.id.place_details);
        placeDetailsScreen.setVisibility(View.GONE);

        favoritesLayout = findViewById(R.id.favorites_layout);
        favoritesLayout.setVisibility(View.GONE);

        mainMenu = findViewById(R.id.main_menu);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Places API with my key.
        Places.initialize(getApplicationContext(), "USE YOUR OWN API KEY");
        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.navigation_map);

        // These places will be put onto buttons for quick search.
        String[] placesToSearchButtons = {"atm", "bus_station", "car_repair", "fire_station",
                "gas_station", "gym", "hospital", "restaurant", "police"};

        TableLayout tableLayout = findViewById(R.id.place_buttons_layout);

        // Creates the grid for the quick search buttons.
        int colNum = 3;
        // Calculate the number of rows
        int rowNum = (placesToSearchButtons.length + colNum - 1) / colNum;

        for (int i = 0; i < rowNum; i++) {
            TableRow tableRow = new TableRow(this);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT);
            tableRow.setLayoutParams(rowParams);

            for (int j = 0; j < colNum; j++) {
                int index = i * colNum + j;
                if (index < placesToSearchButtons.length) {
                    Button button = new Button(this);
                    button.setText(placesToSearchButtons[index].replace("_", " "));
                    button.setTextSize(35);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    params.weight = 1;
                    button.setLayoutParams(params);

                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (locationCallback.userLocation != null) {
                                myMap.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(locationCallback.userLocation, 11));
                                myMap.clear();
                                mapScreen.setVisibility(View.VISIBLE);
                                menuItem.setChecked(true);
                                searchNearbyPlaces(placesToSearchButtons[index]);
                            }
                        }
                    });

                    tableRow.addView(button);
                }
            }
            tableLayout.addView(tableRow);
        }

        // Gives the user many choices to search for. They can find choices by typing.
        AutoCompleteTextView autoCompleteSearch = findViewById(R.id.auto_complete_text);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, placeTypes);
        autoCompleteSearch.setAdapter(adapter);
        autoCompleteSearch.setBackgroundColor(Color.BLACK);
        autoCompleteSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId
                    == EditorInfo.IME_ACTION_DONE) {
                myMap.clear();
                myMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(locationCallback.userLocation, 11));
                mapScreen.setVisibility(View.VISIBLE);
                String query = autoCompleteSearch.getText().toString();
                menuItem.setChecked(true);
                searchNearbyPlaces(query);
                return true;
            }
            return false;
        });
        autoCompleteSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autoCompleteSearch.setRawInputType(InputType.TYPE_CLASS_TEXT);

        // Creates the textLayout which is only headings for the favorites menu.
        LinearLayout textLayout = findViewById(R.id.heading_layout); // Name and Address
        textLayout.setVisibility(View.GONE);

        // Controls the bottom navigation. Can go between the main menu, the map, and the favorites
        // menu.
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_main_menu) {
                mainMenu.setVisibility(View.VISIBLE);
                mapScreen.setVisibility(View.GONE);
                placeDetailsScreen.setVisibility(View.GONE);
                favoritesLayout.setVisibility(View.GONE);
                textLayout.setVisibility(View.GONE);
                return true;
            } else if (item.getItemId() == R.id.navigation_map) {
                mainMenu.setVisibility(View.GONE);
                mapScreen.setVisibility(View.VISIBLE);
                placeDetailsScreen.setVisibility(View.GONE);
                favoritesLayout.setVisibility(View.GONE);
                textLayout.setVisibility(View.GONE);
                return true;
            } else if (item.getItemId() == R.id.navigation_favorites) {
                mainMenu.setVisibility(View.GONE);
                mapScreen.setVisibility(View.GONE);
                placeDetailsScreen.setVisibility(View.GONE);
                favoritesLayout.setVisibility(View.VISIBLE);
                textLayout.setVisibility(View.VISIBLE);
                updateFavoritesMenu();
                return true;
            }
            return false;
        });

        // An invisible overlay that removes the place details screen by clicking anywhere.
        overlayView = findViewById(R.id.overlayView);
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the placeDetails layout and the overlay.
                placeDetailsScreen.setVisibility(View.GONE);
                overlayView.setVisibility(View.GONE);
            }
        });


        // An invisible overlay that waits for user to grant permissions.
        permissionView = findViewById(R.id.permission_view_button);


        // Loads the favorites list if one exists.
        loadPlacesList();
    }

    /**
     * This method runs when the map is displayed.
     */
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        myMap.setOnMarkerClickListener(marker -> {
            showPlaceDetails(marker);
            return true;
        });
    }

    /**
     * Gets and holds the user's location.
     */
    private class LocationCallback extends com.google.android.gms.location.LocationCallback {
        private LatLng userLocation;

        /**
         * Gets the user's location.
         */
        @Override
        public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
            if (locationResult.getLastLocation() != null) {
                double userLatitude = locationResult.getLastLocation().getLatitude();
                double userLongitude = locationResult.getLastLocation().getLongitude();
                userLocation = new LatLng(userLatitude, userLongitude);
            } else {
                Toast.makeText(MainActivity.this, "User location not available",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Tracks the user's device (gets their location).
     */
    private void startLocationUpdates() {
        // Requests user's location with high accuracy every 10 seconds.
        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        // If permissions are not permitted return. Else get location.
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Requests update to locationCallback using the locationRequest.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Search for a specific place.
     *
     * @param search what is being searched for.
     */
    private void searchAndDisplayPlace(String search) {
        // These are the fields that will be retrieved.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS);

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(search)
                .build();

        PlacesClient placesClient = Places.createClient(this);

        // Finds a place based on the user's search.
        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((response) -> {
                    if (!response.getAutocompletePredictions().isEmpty()) {
                        AutocompletePrediction prediction =
                                response.getAutocompletePredictions().get(0);
                        String placeId = prediction.getPlaceId();

                        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest
                                .builder(placeId, placeFields).build();

                        placesClient.fetchPlace(fetchPlaceRequest)
                                .addOnSuccessListener((fetchPlaceResponse) -> {
                                    Place place = fetchPlaceResponse.getPlace();
                                    LatLng placeLatLng = place.getLatLng();

                                    // Add a marker for the place on the map.
                                    assert placeLatLng != null;
                                    myMap.addMarker(new MarkerOptions()
                                            .position(placeLatLng)
                                            .title(place.getName())
                                            .snippet(place.getAddress()));

                                    // Move the camera to the searched place
                                    myMap.moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(placeLatLng, 15));
                                })
                                .addOnFailureListener((exception) -> {
                                    Toast.makeText(MainActivity.this,
                                            "Failed to fetch place details",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Place not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener((exception) -> {
                    Toast.makeText(MainActivity.this,
                            "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Searches nearby locations
     * @param search the type of place being searched for.
     */
    private void searchNearbyPlaces(String search) {
        OkHttpClient client = new OkHttpClient();
        String apiKey = "AIzaSyBkW_gvVXS5GKWDXIG71wCrxGMu4I0cbtw";
        String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
        int radius = 50000; // Search radius

        // Creates the search url.
        String url = baseUrl + "?location=" + locationCallback.userLocation.latitude + "," +
                locationCallback.userLocation.longitude +
                "&radius=" + radius +
                "&type=" + search +
                "&key=" + apiKey;

        // Sets url to the next page if one is available.
        if (nextPageToken != null) {
            url += "&pagetoken=" + nextPageToken;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            // Does this when page is successfully retrieved.
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    // Parse the JSON response and handle the data, including checking for a
                    // next_page_token
                    try {
                        sleep(2000); // A time gap is needed for successful page retrieval.
                        parseJSONResponse(responseBody, search);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * Parses the JSON Response
     */
    private void parseJSONResponse(String responseBody, String search) {
        // Creates a new runnable for the search
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Tries an html request to get a page of locations from the search url.
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String status = jsonObject.getString("status");

                    if (status.equals("OK")) {
                        JSONArray results = jsonObject.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject placeData = results.getJSONObject(i);
                            String placeName = placeData.getString("name");
                            double placeLat = placeData.getJSONObject("geometry")
                                    .getJSONObject("location").getDouble("lat");
                            double placeLng = placeData.getJSONObject("geometry")
                                    .getJSONObject("location").getDouble("lng");
                            String placeAddress = placeData.getString("vicinity");
                            String placeId = placeData.getString("place_id");

                            addMarker(placeLat, placeLng, placeName, placeAddress, placeId);
                        }

                        // Rerun the search to add more markers if there is a next page.
                        if (jsonObject.has("next_page_token")) {
                            nextPageToken = jsonObject.getString("next_page_token");
                            searchNearbyPlaces(search);
                        } else {
                            nextPageToken = null;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Draws a route on the map between to places.
     *
     * @param origin The start of the route
     * @param destination The end of the route
     */
    private void drawRouteOnMap(LatLng origin, LatLng destination) {
        // Sets up the request for the Directions API
        String apiKey = "AIzaSyBkW_gvVXS5GKWDXIG71wCrxGMu4I0cbtw";
        GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();

        com.google.maps.model.LatLng gmapsOrigin = new
                com.google.maps.model.LatLng(origin.latitude, origin.longitude);
        com.google.maps.model.LatLng gmapsDestination = new
                com.google.maps.model.LatLng(destination.latitude, destination.longitude);

        DirectionsApiRequest req = DirectionsApi.newRequest(context);
        req.origin(gmapsOrigin);
        req.destination(gmapsDestination);
        req.mode(TravelMode.DRIVING);

        try {
            DirectionsResult result = req.await();
            DirectionsRoute route = result.routes[0];
            EncodedPolyline encodedPolyline = route.overviewPolyline;

            // Decode the polyline
            List<com.google.maps.model.LatLng> decodedPolyline = encodedPolyline.decodePath();

            // Convert the List<com.google.maps.model.LatLng> to List<LatLng>
            List<LatLng> androidLatLngs = new ArrayList<>();
            for (com.google.maps.model.LatLng gmapsLatLng : decodedPolyline) {
                androidLatLngs.add(new LatLng(gmapsLatLng.lat, gmapsLatLng.lng));
            }

            // Clear any existing route on the map
            if (routeLine != null) {
                routeLine.remove();
            }

            // Draw the new route on the map
            routeLine = myMap.addPolyline(new PolylineOptions()
                    .addAll(androidLatLngs)
                    .color(Color.BLUE)
                    .width(10));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a marker on the map.
     */
    @SuppressLint("PotentialBehaviorOverride")
    private void addMarker(double lat, double lng, String name, String address, String placeId) {
        Marker marker = myMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(name)
                .snippet(address));

        assert marker != null;
        marker.setTag(placeId);

        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @SuppressLint("PotentialBehaviorOverride")
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                showPlaceDetails(marker);
                LatLng markerLatLng = marker.getPosition();
                LatLng userLatLng = locationCallback.userLocation;
                drawRouteOnMap(userLatLng, markerLatLng);
                return true;
            }
        });
    }

    /**
     * Shows the details of a specific place.
     */
    private void showPlaceDetails(Marker marker) {
        String placeId = Objects.requireNonNull(marker.getTag()).toString();
        fetchPlaceDetails(placeId);

        getDrivingDirections(locationCallback.userLocation, marker.getPosition());
        getWalkingDirections(locationCallback.userLocation, marker.getPosition());
    }

    /**
     * Fetches details of a specific place.
     */
    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHOTO_METADATAS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.ID,
                Place.Field.LAT_LNG
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    showPlaceDetailsBox(place);
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(this, "Failed to fetch place details",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays the place details in a box.
     */
    @SuppressLint("SetTextI18n")
    private void showPlaceDetailsBox(Place place) {

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFavoritePlace(place);
            }
        });

        TextView placeName = findViewById(R.id.place_name);
        TextView address = findViewById(R.id.address);
        TextView rating = findViewById(R.id.rating);
        TextView totalRatings = findViewById(R.id.total_ratings);
        ImageView placeImage = findViewById(R.id.place_image);

        placeName.setText(place.getName());
        address.setText("Address: " + place.getAddress());
        rating.setText("Rating: ");
        totalRatings.setText("Total user ratings: " + place.getUserRatingsTotal());

        // Sets the text correctly.
        if (place.getRating() != null) {
            ratingBar.setRating((place.getRating().floatValue()));
            ratingBar.setVisibility(View.VISIBLE);
        }
        if (place.getRating() == null) {
            address.setText("Address: " + place.getAddress());
            rating.setText("Rating: Not available");
            ratingBar.setVisibility(View.GONE);
            totalRatings.setText("Total user ratings: " + place.getUserRatingsTotal());
        }
        if (place.getUserRatingsTotal() == null) {
            address.setText("Address: " + place.getAddress());
            rating.setText("Rating: ");
            ratingBar.setVisibility(View.VISIBLE);
            totalRatings.setText("Total user ratings: Not Available");
        }
        if (place.getRating() == null && place.getUserRatingsTotal() == null) {
            address.setText("Address: " + place.getAddress());
            rating.setText("Rating: Not Available");
            ratingBar.setVisibility(View.GONE);
            totalRatings.setText("Total user ratings: Not Available");

        }

        // Requests a photo if one is available and displays it.
        if (place.getPhotoMetadatas() != null && place.getPhotoMetadatas().size() > 0) {
            // Fetch the first photo
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Build a request for the photo
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Maximum width of the photo
                    .setMaxHeight(500) // Maximum height of the photo
                    .build();

            placesClient.fetchPhoto(photoRequest)
                    .addOnSuccessListener((fetchPhotoResponse) -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bitmap);
                        placeImage.setImageBitmap(bitmap);
                    })
                    .addOnFailureListener(exception -> {
                        // Failed to fetch photo
                        Toast.makeText(this, "Failed to fetch photo",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            placeImage.setImageBitmap(null);
        }
        placeDetailsScreen.setVisibility(View.VISIBLE);
        overlayView.setVisibility(View.VISIBLE);
    }

    /**
     * Get length of time it takes to walk and drive to a location.
     */
    private void getDirections(LatLng origin, LatLng destination, TravelMode mode) {
        String apiKey = "AIzaSyBkW_gvVXS5GKWDXIG71wCrxGMu4I0cbtw";
        GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();

        com.google.maps.model.LatLng gmapsOrigin = new
                com.google.maps.model.LatLng(origin.latitude, origin.longitude);
        com.google.maps.model.LatLng gmapsDestination = new
                com.google.maps.model.LatLng(destination.latitude, destination.longitude);

        DirectionsApiRequest req = new DirectionsApiRequest(context);
        req.origin(gmapsOrigin);
        req.destination(gmapsDestination);
        req.mode(mode);

        try {
            DirectionsResult result = req.await();

            if (result.routes != null && result.routes.length > 0) {
                String durationText = result.routes[0].legs[0].duration.humanReadable;

                if (mode == TravelMode.DRIVING) {
                    Log.d("DURATION", "Driving Time: " + durationText);
                } else if (mode == TravelMode.WALKING) {
                    Log.d("DURATION", "Walking Time: " + durationText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets time for driving to a destination.
     */
    private void getDrivingDirections(LatLng origin, LatLng destination) {
        getDirections(origin, destination, TravelMode.DRIVING);
    }

    /**
     * Gets time for waling to a destination.
     */
    private void getWalkingDirections(LatLng origin, LatLng destination) {
        getDirections(origin, destination, TravelMode.WALKING);
    }

    /**
     * Saves a place to the favorites list.
     *
     * @param place place to add to favorites.
     */
    private void saveFavoritePlace(Place place) {

        // Checks if the place is already in favorites list.
        for (Place favorite : favoriteList) {
            Log.d("Favorite", "favorite.address: " + favorite.getAddress());
            Log.d("Place", "place.address: " + place.getAddress());
            if (Objects.equals(favorite.getAddress(), place.getAddress())) {
                // Place is already a favorite
                Toast.makeText(this, "Place is already in favorites",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Add the new place to favorites
        favoriteList.add(place);
        favoriteIDList.add(place.getId());

        // Notify the user that the place has been added to favorites
        Toast.makeText(this, "Place added to favorites", Toast.LENGTH_SHORT).show();

        // Update the favorites menu to display the favorites
        updateFavoritesMenu();
    }

    /**
     * Updates the display of the favorites menu.
     */
    @SuppressLint("PotentialBehaviorOverride")
    private void updateFavoritesMenu() {
        // Clear the existing rows in the table layout
        favoritesLayout.removeAllViews();

        // Iterate through the favorite places and add them to the table layout
        for (int i = 0; i < favoriteList.size(); i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // Creates TextView for place name
            TextView nameTextView = new TextView(this);
            nameTextView.setText(favoriteList.get(i).getName());
            nameTextView.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f));
            nameTextView.setTextSize(30);
            nameTextView.setTextColor(Color.BLACK);
            row.addView(nameTextView);

            // Creates TextView for place address
            TextView addressTextView = new TextView(this);
            addressTextView.setText(favoriteList.get(i).getAddress());
            addressTextView.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f));
            addressTextView.setTextSize(30);
            addressTextView.setTextColor(Color.RED);
            row.addView(addressTextView);

            // Create ImageButton to remove favorites
            ImageButton removeFavorite = new ImageButton(this);
            removeFavorite.setImageResource(R.drawable.remove_favorite);
            removeFavorite.setLayoutParams(new TableRow.LayoutParams(150, 150));
            // Scale the image to fit the ImageButton
            removeFavorite.setScaleType(ImageView.ScaleType.FIT_CENTER);
            // Set background color to transparent
            removeFavorite.setBackgroundColor(Color.TRANSPARENT);
            int finalI = i;
            removeFavorite.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    favoriteList.remove(finalI);
                    favoriteIDList.remove(finalI);
                    updateFavoritesMenu();
                }
            });
            row.addView(removeFavorite);

            // Creates a button to show a favorite place on the map.
            ImageButton showFavoriteButton = new ImageButton(this);
            showFavoriteButton.setImageResource(R.drawable.favorite_icon);
            showFavoriteButton.setLayoutParams(new TableRow.LayoutParams(150, 150));
            showFavoriteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            showFavoriteButton.setBackgroundColor(Color.TRANSPARENT);
            int finalI1 = i;
            showFavoriteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    myMap.clear();
                    Marker marker = myMap.addMarker(new MarkerOptions()
                            .position(Objects.requireNonNull(favoriteList.get(finalI1).getLatLng()))
                            .title(favoriteList.get(finalI1).getName())
                            .snippet(favoriteList.get(finalI1).getAddress()));

                    // Set the Place ID as the tag for the marker
                    assert marker != null;
                    marker.setTag(favoriteList.get(finalI1).getId());

                    myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @SuppressLint("PotentialBehaviorOverride")
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            showPlaceDetails(marker);
                            LatLng markerLatLng = marker.getPosition();
                            LatLng userLatLng = locationCallback.userLocation;
                            drawRouteOnMap(userLatLng, markerLatLng);
                            return true;
                        }
                    });

                    showPlaceDetails(marker);
                    drawRouteOnMap(locationCallback.userLocation, marker.getPosition());
                    mapScreen.setVisibility(View.VISIBLE);
                    placeDetailsScreen.setVisibility(View.GONE);
                    favoritesLayout.setVisibility(View.GONE);
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                            (marker.getPosition(), 11));
                }
            });
            row.addView(showFavoriteButton);

            // Add the TableRow to the TableLayout
            favoritesLayout.addView(row);

            // Add a horizontal line as a divider between rows
            View line = new View(this);
            line.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    2));
            line.setBackgroundColor(Color.BLACK);
            favoritesLayout.addView(line);
        }

        savePlacesList();
    }

    /**
     * Saves the list of places to a JSON file.
     */
    private void savePlacesList() {
        Gson gson = new Gson();
        String favoriteListIDJson = gson.toJson(favoriteIDList);
        SharedPreferences sharedPreferences =
                getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("favoriteListIDJson", favoriteListIDJson);
        editor.apply();
    }

    /**
     * Loads the list of places from a JSON file.
     */
    private void loadPlacesList() {
        List<String> tempList = new ArrayList<>();
        try {
            SharedPreferences sharedPreferences = getSharedPreferences
                    ("MyPreferences", MODE_PRIVATE);
            String favoriteListIDJson = sharedPreferences.getString
                    ("favoriteListIDJson", "");

            if (!favoriteListIDJson.isEmpty()) {
                Gson gson = new Gson();
                tempList = gson.fromJson(favoriteListIDJson, new TypeToken<List<String>>() {
                }.getType());
            }
        } catch (JsonSyntaxException ignored) {
        }

        // Fields to return.
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHOTO_METADATAS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.ID,
                Place.Field.LAT_LNG
        );
        PlacesClient tempPlacesClient = Places.createClient(this);

        // Loads the lists.
        for (String placeId : tempList) {
            favoriteIDList.add(placeId);
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

            tempPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                favoriteList.add(place);
            }).addOnFailureListener((exception) -> {
            });
        }
    }

    /**
     * Shows a Snack bar requesting the user to turn on location.
     */
    private void showPermissionSnackbar() {
        Snackbar.make(findViewById(android.R.id.content),
                        "Location permission is required for this app to function properly.",
                        Snackbar.LENGTH_INDEFINITE)
                .setAction("Grant Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                1);
                    }
                })
                .show();
    }

    /**
     * Opens the app settings.
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Does this when the result of requesting permission has been obtained.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                permissionView.setVisibility(View.GONE);
                mainMenu.setVisibility(View.VISIBLE);
                myMap.setMyLocationEnabled(true);
                locationCallback = new LocationCallback();
                startLocationUpdates();
            } else {
                // Permission denied.
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // The user has selected "Don't ask again."
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Location permission is required for this app. " +
                                            "Please grant the permission in the app settings.",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Open Settings", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    openAppSettings();
                                }
                            })
                            .show();
                } else {
                    // The user denied the permission. Show the permission Snackbar again.
                    showPermissionSnackbar();
                }
            }
        }
    }

    /**
     * Checks if location is turned on start.
     */
    @Override
    protected void onStart() {
        super.onStart();
        int permissionStatus = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        Log.d("PermissionStatus", "Location Permission Status: " + permissionStatus);
        // Location turned off
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            permissionView.setVisibility(View.VISIBLE);
            mainMenu.setVisibility(View.GONE);
            showPermissionSnackbar();
            // Location turned on
        } else {
            permissionView.setVisibility(View.GONE);
            mainMenu.setVisibility(View.VISIBLE);
            locationCallback = new LocationCallback();
            startLocationUpdates();
        }
    }
}
