package com.example.application;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Map")
@Route
public class MainView extends VerticalLayout {

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

        // Ask permission to use the location adn pass
        // location to the server
        getElement().executeJs("""
            var el = this;
            // https://developer.mozilla.org/en-US/docs/Web/API/Geolocation/watchPosition
            navigator.geolocation.watchPosition(
              position => {
                el.$server.updateMyLocation(position.coords.latitude,position.coords.longitude);
              },
              error => {
                 // those ever happen for the great developers :-)
              },
              {enableHighAccuracy: true, timeout: 5000, maximumAge: 1000 }
            );
        """);
    }

    /**
     * Called by the browser on new geolocation updates
     * @param lat latitude
     * @param lon longitude
     */
    @ClientCallable
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
