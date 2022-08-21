package com.example.application.geolocation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DomListenerRegistration;

public class Geolocation {

    private static ObjectMapper om = new ObjectMapper();
    private DomListenerRegistration geoupdate;
    private DomListenerRegistration geoerror;

    private UI ui;

    private Integer id;

    public interface UpdateListener {
        void geolocationUpdate(GeolocationEvent event);
    }

    public interface ErrorListener {
        void geolocationError(String browserError);
    }

    public static Geolocation watchPosition(UpdateListener listener, ErrorListener errorListener) {
        return watchPosition(listener, errorListener, new GeolocationOptions());
    }

    public static Geolocation watchPosition(UpdateListener listener, ErrorListener errorListener, GeolocationOptions options) {
        UI ui = UI.getCurrent();
        return watchPosition(ui, listener, errorListener, options);
    }

    public static Geolocation watchPosition(UI ui, UpdateListener listener, ErrorListener errorListener, GeolocationOptions options) {
        return checkPosition(ui,listener,errorListener,options,false);
    }

    public static void getPosition(UpdateListener listener, ErrorListener errorListener, GeolocationOptions options) {
        checkPosition(UI.getCurrent(), listener, errorListener, options, true);
    }
    public static void getPosition(UI ui, UpdateListener listener, ErrorListener errorListener, GeolocationOptions options) {
        checkPosition(ui,listener,errorListener,options, true);
    }

    public static void getPosition(UpdateListener listener, ErrorListener errorListener) {
        getPosition(listener, errorListener, new GeolocationOptions());
    }

    private static Geolocation checkPosition(UI ui, UpdateListener listener, ErrorListener errorListener, GeolocationOptions options, boolean get) {
        Geolocation geolocation = new Geolocation();
        geolocation.ui = ui;

        String method = get ? "getPosition" : "watchPosition";

        /*
         * Implementation note: would be much nicer/simpler if one could
         * simply call a server side method/lambda as callback
         * function, instead of arbitrary communicating with
         * custom dom events.
         *
         * This is a limitation in Vaadin, that don't matter with
         * most of our core components as they are heavily based
         * on web components. But with non-component related and
         * non-webcomponent component related client side integrations
         * call back support would be great.
         */

        geolocation.geoupdate = ui.getElement().addEventListener("geoupdate", e -> {
            String detail = e.getEventData().getString("event.detail");
            try {
                GeolocationEvent geolocationEvent = om.readValue(detail, GeolocationEvent.class);
                listener.geolocationUpdate(geolocationEvent);
                if(get) {
                    geolocation.clearListeners();
                }
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
        geolocation.geoupdate.addEventData("event.detail");

        geolocation.geoerror = ui.getElement().addEventListener("geoerror", e -> {
            errorListener.geolocationError(e.getEventData().getString("event.detail"));
            if(get) {
                geolocation.clearListeners();
            }
        });
        geolocation.geoerror.addEventData("event.detail");

        try {
            ui.getElement().executeJs("""
                    var el = this;"""
                    + "return navigator.geolocation." + method + "(" +
                    """
                              p => {
                                const event = new CustomEvent('geoupdate', { 
                                    detail: JSON.stringify(
                                    {
                                        coords : {
                                            longitude : p.coords.longitude,
                                            latitude : p.coords.latitude,
                                            accuracy : p.coords.accuracy,
                                            altitude : p.coords.altitude,
                                            altitudeAccuracy : p.coords.altitudeAccuracy,
                                            heading : p.coords.heading,
                                            speed : p.coords.speed
                                        },
                                        timestamp: p.timestamp
                                    })
                                });
                                el.dispatchEvent(event);
                              },
                              e => {
                                const event = new CustomEvent('geoerror', {detail: e.message});
                                el.dispatchEvent(event);
                              },
                              JSON.parse($0)
                            );
                            """, om.writeValueAsString(options)).then(Integer.class, s -> geolocation.setId(s));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return geolocation;
    }

    private void setId(Integer id) {
        this.id = id;
    }

    public void cancel() {
        ui.getElement().executeJs("""
            navigator.geolocation.clearWatch($1);
        """, id);
    };

    private void clearListeners() {
        if(geoerror != null) {
            geoerror.remove();
            geoerror = null;
        }
        if(geoupdate != null) {
            geoupdate.remove();
            geoupdate = null;
        }
    }

}
