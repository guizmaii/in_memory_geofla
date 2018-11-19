package com.guizmaii.geofla

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator
import org.locationtech.jts.geom._
import org.locationtech.jts.index.quadtree.Quadtree
import org.locationtech.jts.io.WKTReader

import scala.io.{Codec, Source}

object Geofla {

  import scala.collection.JavaConverters._

  final case class Commune(
      geometry: Geometry,
      locator: IndexedPointInAreaLocator,
      idGeofla: String,
      codeCommune: String,
      inseeCommune: String,
      nomCommune: String,
      statut: String,
      xChefLieu: String,
      yChefLieu: String,
      xCentroid: String,
      yCentroid: String,
      zMoyen: String,
      superficie: String,
      population: String,
      codeArr: String, // arr ?
      codeDepartement: String,
      nomDepartement: String,
      codeRegion: String,
      nomRegion: String
  )

  private[this] final val lines  = Source.fromResource("COMMUNE.csv")(Codec.UTF8).getLines().drop(1)
  private[this] final val reader = new WKTReader()

  private[this] final val geometries =
    lines
      .map(_.split("\"").drop(1))
      .map {
        case Array(wkt: String, rest: String) => wkt :: rest.split(',').drop(1).toList
      }
      .map { list =>
        val geom = reader.read(list(0))
        Commune(
          geometry = geom,
          locator = new IndexedPointInAreaLocator(geom),
          idGeofla = list(1),
          codeCommune = list(2),
          inseeCommune = list(3),
          nomCommune = list(4),
          statut = list(5),
          xChefLieu = list(6),
          yChefLieu = list(7),
          xCentroid = list(8),
          yCentroid = list(9),
          zMoyen = list(10),
          superficie = list(11),
          population = list(12),
          codeArr = list(13),
          codeDepartement = list(14),
          nomDepartement = list(15),
          codeRegion = list(16),
          nomRegion = list(17)
        )
      }
      .toArray

  private[this] final val parGeometries = geometries.par

  private[this] final val tree = new Quadtree()
  geometries.foreach(g => tree.insert(g.geometry.getEnvelopeInternal, g))

  private[this] final val geometryFactory = new GeometryFactory

  def withSpatialIndexFindBy(latitude: Double, longitude: Double): Option[Commune] = {
    val point: Geometry = geometryFactory.createPoint(new Coordinate(longitude, latitude))

    tree
      .query(point.getEnvelopeInternal)
      .asScala
      .find(_.asInstanceOf[Commune].geometry.contains(point))
      .asInstanceOf[Option[Commune]]
  }

  def arrayFindBy(latitude: Double, longitude: Double): Option[Commune] = {
    val coordinate = new Coordinate(longitude, latitude)

    geometries.find { commune =>
      val location = commune.locator.locate(coordinate)
      location == 0 || location == 1
    }
  }

  def parArrayWithoutLocatorFindBy(latitude: Double, longitude: Double): Option[Commune] = {
    val coordinate      = new Coordinate(longitude, latitude)
    val point: Geometry = geometryFactory.createPoint(coordinate)

    parGeometries.find(_.geometry.contains(point))
  }

  def parArrayFindBy(latitude: Double, longitude: Double): Option[Commune] = {
    val coordinate = new Coordinate(longitude, latitude)

    parGeometries.find { commune =>
      val location = commune.locator.locate(coordinate)
      location == 0 || location == 1
    }
  }

  def findByWithSpatialIndex_0(latitude: Double, longitude: Double): Option[Commune] = {
    val coordinate      = new Coordinate(longitude, latitude)
    val point: Geometry = geometryFactory.createPoint(coordinate)

    tree
      .query(point.getEnvelopeInternal)
      .asScala
      .map(_.asInstanceOf[Commune])
      .find { commune =>
        val location = commune.locator.locate(coordinate)
        location == 0 || location == 1
      }
  }

  def findByWithSpatialIndex_1(latitude: Double, longitude: Double): Option[Commune] = {
    val coordinate      = new Coordinate(longitude, latitude)
    val point: Geometry = geometryFactory.createPoint(coordinate)

    tree
      .query(point.getEnvelopeInternal)
      .asScala
      .find { commune =>
        val location = commune.asInstanceOf[Commune].locator.locate(coordinate)
        location == 0 || location == 1
      }
      .asInstanceOf[Option[Commune]]
  }

}
