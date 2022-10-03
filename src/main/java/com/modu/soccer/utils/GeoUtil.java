package com.modu.soccer.utils;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class GeoUtil {
	private static final GeometryFactory geometryFactory = new GeometryFactory();

	public static Point createPoint(Double longitude, Double latitude) {
		return geometryFactory.createPoint(new Coordinate(longitude, latitude));

	}
}
