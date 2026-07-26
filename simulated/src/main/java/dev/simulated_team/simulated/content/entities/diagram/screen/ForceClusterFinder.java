package dev.simulated_team.simulated.content.entities.diagram.screen;

import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;

public class ForceClusterFinder {
    private static final double CLUSTER_SEPARATION_THRESHOLD = 0.4; //radians
    public static List<Cluster> passThrough(final List<QueuedForceGroup.PointForce> forces)
    {
        final List<Cluster> clusters = new ArrayList<>(forces.size());

        for (final QueuedForceGroup.PointForce force : forces)
            clusters.add(new Cluster(new Vector3d(force.point()),new Vector3d(force.force()),new MutableInt(1)));

        return clusters;
    }
    public static List<Cluster> getMergedClusters(final List<QueuedForceGroup.PointForce> forces) {
        final List<Cluster> clusters = new ArrayList<>();
        if(forces.isEmpty())
            return clusters;
        final List<ClusteredForce> clusteredForces = new ArrayList<>();
        for (final QueuedForceGroup.PointForce force : forces) {
            clusteredForces.add(new ClusteredForce(force.point(),force.force(),new MutableInt()));
        }

        while(tryAddCluster(clusters,clusteredForces))
        {
            while(!groupArrows(clusters,clusteredForces))
            {
                organizeClusters(clusters,clusteredForces);
            }
        }

        organizeClusters(clusters,clusteredForces);
        finalizeClusters(clusters,clusteredForces);

        return clusters;
    }

    static boolean tryAddCluster(final List<Cluster> clusters, final List<ClusteredForce> forces)
    {
        if(clusters.isEmpty()) {
            final Cluster c = new Cluster(new Vector3d(),new Vector3d(),new MutableInt());
            for (final ClusteredForce force : forces)
                c.force.add(Math.abs(force.force.x()),Math.abs(force.force.y()),Math.abs(force.force.z()));
            c.force.normalize();
            clusters.add(c);
            return true;
        }
        double maxDistance = -1;
        ClusteredForce index = null;
        for (final ClusteredForce force : forces)
        {
            final double d = getVariance(clusters.get(force.getIndex()).force,force.force);
            if(d > maxDistance)
            {
                maxDistance = d;
                index = force;
            }
        }
        //generate a new cluster for the force that is furthest from all currest clusters,
        // along as that distance is more than the threshold
        if(index != null && maxDistance > CLUSTER_SEPARATION_THRESHOLD*CLUSTER_SEPARATION_THRESHOLD)
        {
            final Cluster c = new Cluster(new Vector3d(),new Vector3d(index.force),new MutableInt());
            clusters.add(c);
            return true;
        }
        return false;
    }

    static boolean groupArrows(final List<Cluster> clusters, final List<ClusteredForce> forces)
    {
        boolean done = true;
        for (final ClusteredForce force : forces)
        {
            final int previousIndex = force.getIndex();
            double minDist = 100;
            for (int i = 0; i < clusters.size(); i++) {
                final double dist = getVariance(force.force,clusters.get(i).force);
                if(dist < minDist)
                {
                    minDist = dist;
                    force.clusterIndex.setValue(i);
                }
            }
            if(previousIndex != force.getIndex())
                done = false;
        }
        return done;
    }

    static void organizeClusters(final List<Cluster> clusters, final List<ClusteredForce> forces)
    {
        for (final Cluster c : clusters) {
            c.force.zero();
            c.groupSize.setValue(0);
        }

        for (final ClusteredForce force : forces) {
            final Cluster c = clusters.get(force.getIndex());
            c.force.add(force.force);
            c.groupSize.increment();
        }
        for(int k = clusters.size()-1;k>=0;k--)
        {
            final Cluster c = clusters.get(k);

            if(c.groupSize.getValue()==0)
            {
                clusters.remove(c);
                for (final ClusteredForce force : forces) {
                    if(force.clusterIndex.getValue()>k)
                        force.clusterIndex.decrement();
                }
            }
        }
    }

    static void finalizeClusters(final List<Cluster> clusters, final List<ClusteredForce> forces)
    {
        for (final ClusteredForce force : forces)
        {
            final Cluster c = clusters.get(force.getIndex());
            c.pos.fma(c.force.dot(force.force)/c.force.lengthSquared(),force.pos);
        }
    }

    static double getVariance(final Vector3dc x, final Vector3dc y)
    {
        final double x2 = x.dot(x);
        final double xy = x.dot(y);
        final double y2 = y.dot(y);
        //return (x2*y2-xy*xy)/(x2*y2);
        return 2*(1-xy/Math.sqrt(x2*y2));
    }

    public record Cluster(Vector3d pos, Vector3d force, MutableInt groupSize) {
    }
    record ClusteredForce(Vector3dc pos, Vector3dc force, MutableInt clusterIndex){
        int getIndex()
        {
            return this.clusterIndex.getValue();
        }
    }
}
