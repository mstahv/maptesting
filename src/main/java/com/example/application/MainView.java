package com.example.application;

import com.example.application.geolocation.Geolocation;
import com.example.application.geolocation.GeolocationOptions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@PageTitle("Map")
@Route
public class MainView extends VerticalLayout {

    private Geolocation geolocation;
    private Map map = new Map();

    MarkerFeature myLocation = null;

    public MainView() {
        setSizeFull();
        setPadding(false);

        Coordinate vaadinHQ = new Coordinate(22.3, 60.452);
        View view = map.getView();
        view.setCenter(vaadinHQ);
        view.setZoom(10);
        addAndExpand(map);

        MarkerFeature markerFeature = new MarkerFeature(vaadinHQ);
        map.getFeatureLayer().addFeature(markerFeature);

        map.addFeatureClickListener(e -> {
            if(e.getFeature() == markerFeature) {
                Notification.show("That's Vaadin HQ!");
            } else if(e.getFeature() == myLocation) {
                Notification.show("That's you!");
            }
        });

        Checkbox enableHighAccuracy = new Checkbox("High accuracy");
        IntegerField timeout = new IntegerField("Timeout");
        IntegerField maximumAge = new IntegerField("Max age");

        List<AbstractField> abstractSinglePropertyFields = Arrays.asList(enableHighAccuracy, timeout, maximumAge);

        Button button = new Button("Start tracking");
        button.addClickListener(e -> {
            if(geolocation == null) {
                geolocation = Geolocation.watchPosition(
                        event -> {
                            System.out.println(Instant.ofEpochMilli(event.getTimestamp()) + ":" + event.getCoords());
                            updateMyLocation(event.getCoords().getLatitude(), event.getCoords().getLongitude());
                        },
                        browserError -> {
                            Notification.show("ERROR: " + browserError);
                        },
                        new GeolocationOptions(enableHighAccuracy.getValue(), timeout.getValue(), maximumAge.getValue())
                );
                button.setText("Stop tracking");
                abstractSinglePropertyFields
                        .forEach(c -> c.setEnabled(false));
            } else {
                geolocation.cancel();
                geolocation = null;
                button.setText("Start tracking");
                abstractSinglePropertyFields
                        .forEach(c -> c.setEnabled(true));
            }
        });

        Button checkOnce = new Button("Check once", e -> {
                Geolocation.watchPosition(
                        event -> {
                            System.out.println(Instant.ofEpochMilli(event.getTimestamp()) + ":" + event.getCoords());
                            updateMyLocation(event.getCoords().getLatitude(), event.getCoords().getLongitude());
                        },
                        browserError -> {
                            Notification.show("ERROR: " + browserError);
                        },
                        new GeolocationOptions(enableHighAccuracy.getValue(), timeout.getValue(), maximumAge.getValue())
                );
        });
        add(new HorizontalLayout(button, checkOnce, enableHighAccuracy, timeout, maximumAge));
    }

    private void updateMyLocation(double lat, double lon) {
        if(myLocation == null) {
            myLocation = new MarkerFeature();
            map.getFeatureLayer().addFeature(myLocation);
        }
        Coordinate coordinate = new Coordinate(lon, lat);
        myLocation.setCoordinates(coordinate);
        map.setCenter(coordinate);
    }

}
