package org.alex73.osmemory.geometry;

import java.io.File;

import junit.framework.TestCase;

import org.alex73.osmemory.MemoryStorage;
import org.alex73.osmemory.XMLReader;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Test for multipolygon geometries creation. Test data described on
 * http://wiki.openstreetmap.org/wiki/Relation:multipolygon.
 */
public class ExtendedRelationTest extends TestCase {

    void test(String file, String wkt) throws Exception {
        MemoryStorage st = new XMLReader().read(new File("src-test/org/alex73/osmemory/geometry/" + file));

        Geometry g = new ExtendedRelation(st.getRelationById(1), st).getArea();

        assertTrue(GeometryHelper.fromWkt(wkt).equals(g));
    }

    @Test
    public void test1() throws Exception {
        test("multipolygon1.xml", "POLYGON ((5 6, 8 11, 12 9, 13 5, 8 2, 5 6), (7 6, 9 5, 10 7, 8 8, 7 6))");
    }

    @Test
    public void test2() throws Exception {
        test("multipolygon2.xml",
                "POLYGON ((5 6, 8 11, 12 9, 13 5, 8 2, 5 6), (8 5, 9 4, 11 6, 9 6, 8 5), (7 7.5, 9 6.5, 10 8.5, 8 9.5, 7 7.5))");
    }

    @Test
    public void test3() throws Exception {
        test("multipolygon3.xml", "POLYGON ((5 6, 8 11, 12 9, 13 5, 8 2, 5 6), (7 6, 9 5, 10 7, 8 8, 7 6))");
    }

    @Test
    public void test4() throws Exception {
        test("multipolygon4.xml",
                "MULTIPOLYGON (((1 6, 4 11, 8 9, 9 5, 4 2, 1 6)), ((9 9, 11 11, 15 8, 12 2, 10 5, 9 9)))");
    }

    @Test
    public void test5() throws Exception {
        test("multipolygon5.xml",
                "MULTIPOLYGON (((1 6, 4 11, 8 9, 9 5, 4 2, 1 6), (3 7, 4 4, 6 4, 7 7, 6 9, 3 7)), ((9 9, 11 11, 15 8, 12 2, 10 5, 9 9), (10 9, 10 6, 12 5, 14 7, 12 9, 10 9)))");
    }

    @Test
    public void test6() throws Exception {
        test("multipolygon6.xml",
                "MULTIPOLYGON (((2 12, 9 11, 10 6, 9 1, 3 2, 1 4, 2 12), (9 3, 8 7, 7 4, 9 3), (3 8, 6 7, 7 9, 5 11, 3 10, 3 8), (6 2, 5 6, 2 6, 2 4, 6 2)), ((11 12, 15 12, 15 6, 11 6, 11 12), (12 11, 13 9, 12 7, 14 7, 14 11, 12 11)), ((12 1, 12 4, 16 4, 16 1, 12 1)))");
    }

    @Test
    public void test7() throws Exception {
        test("multipolygon7.xml",
                "MULTIPOLYGON (((2 6, 6 12, 13 11, 14 4, 9 1, 2 6), (4 6, 10 2, 12 6, 8 11, 4 6)), ((6 6, 7 7, 9 7, 8 5, 6 6)))");
    }

    @Test
    public void test8() throws Exception {
        // 'Touching inner rings' always fail
        //test("multipolygon8.xml",
        //        "POLYGON ((5 6, 8 11, 12 9, 13 5, 8 2, 5 6), (7 6, 9 5, 10 6, 11 7, 9 8, 8 7, 7 6))");
    }

    @Test
    public void test9() throws Exception {
        try {
            test("multipolygon9.xml", "");
            fail();
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().startsWith("Non-closed line"));
        }
    }

    @Test
    public void test10() throws Exception {
        try {
            test("multipolygon10.xml", "");
            fail();
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().startsWith("Non-closed line"));
        }
    }
}
