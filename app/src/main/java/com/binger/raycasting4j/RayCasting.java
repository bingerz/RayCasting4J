package com.binger.raycasting4j;

import java.util.ArrayList;

/**
 * Created by hanbing on 15/9/23.
 * Ray-Casting 算法，判断某点是否在任意多变形内部还是外部
 * 此多边形可以是凸多边形也可以是凹多边形
 */
public class RayCasting {

    public Vector mIntersect;

    public boolean outOfChina(double lat, double lon, ArrayList<Vector> coordinates) {
        Double tol = 1E-10;
        Vector target = new Vector(lat, lon);
        return inside(target, coordinates, tol) < 0;
    }

    public Vector vSub(Vector a, Vector b) {
        Vector c = new Vector();
        c.x = a.x - b.x;
        c.y = a.y - b.y;
        return c;
    }

    public Vector vAdd(Vector a, Vector b) {
        Vector c = new Vector();
        c.x = a.x + b.x;
        c.y = a.y + b.y;
        return c;
    }

    public double vDot(Vector a, Vector b) {
        return a.x * b.x + a.y * b.y;
    }

    public double vCross(Vector a, Vector b) {
        return a.x * b.y - a.y * b.x;
    }

    /* return a + s * b */
    public Vector vMAdd(Vector a, double s, Vector b) {
        Vector c = new Vector();
        c.x = a.x + s * b.x;
        c.y = a.y + s * b.y;
        return c;
    }

    /**
     * check if x0->x1 edge crosses y0->y1 edge. dx = x1 - x0, dy = y1 - y0, then
     * solve  x0 + a * dx == y0 + b * dy with a, b in real cross both sides with dx, then:
     * (remember, cross product is a scalar) x0 X dx = y0 X dx + b * (dy X dx)
     * similarly, x0 X dy + a * (dx X dy) == y0 X dy
     * there is an intersection iff 0 <= a <= 1 and 0 <= b <= 1
     * returns: 1 for intersect, -1 for not, 0 for hard to say
     * (if the intersect point is too close to y0 or y1)
     */
    public int intersect(Vector x0, Vector x1, Vector y0, Vector y1, double tol, boolean sect) {
        Vector dx = vSub(x1, x0), dy = vSub(y1, y0);
        double d = vCross(dy, dx), a;
        if (d == 0) {
            return 0; /* edges are parallel */
        }
        a = (vCross(x0, dx) - vCross(y0, dx)) / d;
        if (sect) {
            mIntersect = vMAdd(y0, a, dy);
        }

        if (a < -tol || a > 1 + tol) {
            return -1;
        }
        if (a < tol || a > 1 - tol) {
            return 0;
        }

        a = (vCross(x0, dy) - vCross(y0, dy)) / d;
        if (a < 0 || a > 1) {
            return -1;
        }

        return 1;
    }

    /**
     * distance between x and nearest point on y0->y1 segment.  if the point
     * lies outside the segment, returns infinity
     */
    double dist(Vector x, Vector y0, Vector y1, double tol) {
        Vector dy = vSub(y1, y0);
        Vector x1 = new Vector();
        int r;

        x1.x = x.x + dy.y;
        x1.y = x.y - dy.x;
        r = intersect(x, x1, y0, y1, tol, true);
        if (r == -1) {
            return Double.MAX_VALUE;
        }
        mIntersect = vSub(mIntersect, x);
        return Math.sqrt(vDot(mIntersect, mIntersect));
    }

    public int inside(Vector v, ArrayList<Vector> vectors, double tol) {
        /* should assert p->n > 1 */
        Vector pv;
        int i, k, crosses, intersectResult;
        double min_x, max_x, min_y, max_y;

        for (i = 0; i < vectors.size(); i++) {
            k = (i + 1) % vectors.size();
            min_x = dist(v, vectors.get(i), vectors.get(k), tol);
            if (min_x < tol) {
                return 0;
            }
        }

        min_x = max_x = vectors.get(0).x;
        min_y = max_y = vectors.get(1).y;

	/* calculate extent of polygon */
        for (i = 0; i < vectors.size(); i++) {
            pv = vectors.get(i);
            if (pv.x > max_x) {
                max_x = pv.x;
            }
            if (pv.x < min_x) {
                min_x = pv.x;
            }
            if (pv.y > max_y) {
                max_y = pv.y;
            }
            if (pv.y < min_y) {
                min_y = pv.y;
            }
        }
        if (v.x < min_x || v.x > max_x || v.y < min_y || v.y > max_y) {
            return -1;
        }

        max_x -= min_x;
        max_x *= 2;
        max_y -= min_y;
        max_y *= 2;
        max_x += max_y;

        Vector e = new Vector();
        while (true) {
            crosses = 0;
            /* pick a rand point far enough to be outside polygon */
            e.x = v.x + (1 + Math.random() / 2) * max_x;
            e.y = v.y + (1 + Math.random() / 2) * max_x;
            for (i = 0; i < vectors.size(); i++) {
                k = (i + 1) % vectors.size();
                intersectResult = intersect(v, e, vectors.get(i), vectors.get(k), tol, false);
                /* picked a bad point, ray got too close to Vector. re-pick */
                if (intersectResult == 0) {
                    continue;
                }
                if (intersectResult == 1) {
                    crosses++;
                }
            }
            if (i == vectors.size()) {
                break;
            }
        }
        return (crosses & 1) == 1 ? 1 : -1;
    }
}
