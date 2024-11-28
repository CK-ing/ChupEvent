package com.example.chupevent;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<Place> {
    private final List<Place> places = new ArrayList<>();
    private final PlacesClient placesClient;

    public PlacesAutoCompleteAdapter(@NonNull Context context, PlacesClient client) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        this.placesClient = client;
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Nullable
    @Override
    public Place getItem(int position) {
        return places.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(places.get(position).getName());
        return textView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    fetchPredictions(constraint.toString());
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    private void fetchPredictions(String query) {
        places.clear();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountry("MY") // Adjust for your target region
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        // Fetch detailed Place data
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(
                                        prediction.getPlaceId(), Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG))
                                .build();

                        placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchResponse -> {
                            places.add(fetchResponse.getPlace());
                            notifyDataSetChanged();
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("Autocomplete", "Error fetching predictions: " + e.getMessage()));
    }
}