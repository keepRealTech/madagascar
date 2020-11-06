package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.lemur.config.MaxMindGeoConfiguration;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Represents the geo ip service.
 */
@Service
public class GeoIpService {

    private final DatabaseReader dbReader;

    /**
     * Constructs the geo ip service.
     *
     * @param maxMindGeoConfiguration {@link MaxMindGeoConfiguration}.
     * @throws IOException File io exception.
     */
    public GeoIpService(MaxMindGeoConfiguration maxMindGeoConfiguration) throws IOException {
        File database = new File(maxMindGeoConfiguration.getDatabasePath());
        this.dbReader = new DatabaseReader.Builder(database).build();
    }

    /**
     * Tries get the country for a given ip.
     *
     * @param ip Ip address.
     * @return Country.
     */
    public Country getCountry(String ip) throws IOException, GeoIp2Exception {
        InetAddress ipAddress = InetAddress.getByName(ip);
        CountryResponse response = this.dbReader.country(ipAddress);

        return response.getCountry();
    }

    /**
     * Checks if the ip is from the united states of america.
     *
     * @param ip Ip address.
     * @return True if from U.S.A.
     */
    public boolean fromStates(String ip) {
        try {
            Country country = this.getCountry(ip);
            return country.getIsoCode().toUpperCase().equals("US");
        } catch (GeoIp2Exception | IOException e) {
            return false;
        }
    }

}
