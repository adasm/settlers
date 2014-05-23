// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Places {
	public static class Place { float x = 0, y = 0, radius = 10; float litAlpha = 0; }
	public static class Warehouse extends Place { }
	public static class Resource extends Place { 
		float omega = 1, cost = 1; 
		
		Body b = null;
		void setup(World world) {
			destroy();
			CircleShape pd = new CircleShape();
			pd.setRadius(radius);
			FixtureDef fd = new FixtureDef();
			fd.shape = pd;
			fd.density = 0;
			fd.filter.groupIndex = -1;
			BodyDef bd = new BodyDef();
			bd.position.set(new Vector2(x, y));
			bd.type = BodyType.StaticBody;
			bd.gravityScale = 0;
			b = world.createBody(bd);
			b.createFixture(fd);
		}
		void update() {
			if(b != null) {
				b.getPosition().x = x;
				b.getPosition().y = y;
				b.getFixtureList().get(0).getShape().setRadius(radius);
			}
		}
		void destroy() {
			if(b != null) {
				b.getWorld().destroyBody(b);
				b = null;
			}
		}
	}
	
}
