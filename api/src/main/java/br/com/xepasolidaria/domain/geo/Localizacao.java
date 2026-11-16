package br.com.xepasolidaria.domain.geo;

import br.com.xepasolidaria.domain.shared.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Localizacao {

    private static final double RAIO_DA_TERRA_KM = 6371.0;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    protected Localizacao() {
    }

    public Localizacao(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new RegraDeNegocioException("Latitude deve estar entre -90 e 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new RegraDeNegocioException("Longitude deve estar entre -180 e 180.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanciaKm(Localizacao destino) {
        double deltaLatitude = Math.toRadians(destino.latitude - latitude);
        double deltaLongitude = Math.toRadians(destino.longitude - longitude);
        double a = Math.pow(Math.sin(deltaLatitude / 2), 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(destino.latitude))
                * Math.pow(Math.sin(deltaLongitude / 2), 2);
        return 2 * RAIO_DA_TERRA_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Localizacao localizacao)) {
            return false;
        }
        return Double.compare(latitude, localizacao.latitude) == 0
                && Double.compare(longitude, localizacao.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
