use crate::collider::LevelCollider;
use crate::scene::ReportedCollisionBuffer;
use crate::ReportedCollision;
use rapier3d::dynamics::RigidBodySet;
use rapier3d::geometry::{ColliderSet, CollisionEvent, ContactPair};
use rapier3d::pipeline::EventHandler;
use rapier3d::prelude::*;
use std::sync::Arc;

#[derive(Clone)]
pub struct SableEventHandler {
    pub reported_collisions: Arc<ReportedCollisionBuffer>,
}

impl EventHandler for SableEventHandler {
    fn handle_collision_event(
        &self,
        _bodies: &RigidBodySet,
        _colliders: &ColliderSet,
        _event: CollisionEvent,
        _contact_pair: Option<&ContactPair>,
    ) {
    }

    fn handle_contact_force_event(
        &self,
        _dt: Real,
        _bodies: &RigidBodySet,
        colliders: &ColliderSet,
        contact_pair: &ContactPair,
        total_force_magnitude: Real,
    ) {
        if total_force_magnitude < 0.1 {
            return;
        }

        let mut batch = Vec::new();

        for manifold in contact_pair.manifolds.iter() {
            for point in manifold.points.iter() {
                let collider_a = colliders.get(contact_pair.collider1).unwrap();
                let collider_b = colliders.get(contact_pair.collider2).unwrap();
                let Some(level_collider_a) = collider_a.shape().as_shape::<LevelCollider>() else {
                    continue;
                };
                let Some(level_collider_b) = collider_b.shape().as_shape::<LevelCollider>() else {
                    continue;
                };

                let local_n1 = manifold.local_n1;
                let local_n2 = manifold.local_n2;

                let local_p1 = point.local_p1;
                let local_p2 = point.local_p2;

                batch.push(ReportedCollision {
                    body_a: level_collider_a.id,
                    body_b: level_collider_b.id,
                    force_amount: total_force_magnitude as f64,
                    local_normal_a: local_n1.as_dvec3(),
                    local_normal_b: local_n2.as_dvec3(),
                    local_point_a: local_p1.as_dvec3(),
                    local_point_b: local_p2.as_dvec3(),
                });
            }
        }

        if !batch.is_empty() {
            self.reported_collisions.borrow_mut().extend(batch);
        }
    }
}
