/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.modules.EmitterModule;

public class ParticleEmitterInstance {

    private final ParticleEffectInstance parentParticleInstance;
	public boolean isComplete = false;
	public ParticleEmitterDescriptor emitterGraph;

	public boolean isAdditive = true;

	Vector2 position = new Vector2();
	float duration;
	float delay;
	float delayTimer;

	public boolean isVisible = true;
	boolean paused = false;
	boolean isContinuous = false;
	boolean isAttached = false;

	public Color tint = new Color(Color.WHITE);

	private EmitterModule emitterModule;

	float rate; // emission rate

	// inner vars
	public float alpha;
	public float particlesToEmmit;

	public boolean initialized = false;

	public Array<Particle> activeParticles = new Array<>();

	private final Pool<Particle> particlePool = new Pool<Particle>() {
		@Override
		protected Particle newObject () {
			return new Particle();
		}
	};
    private boolean isStopped = false;

    public ParticleEmitterInstance (ParticleEmitterDescriptor moduleGraph, ParticleEffectInstance particleEffectInstance) {
		this.emitterGraph = moduleGraph;
        parentParticleInstance = particleEffectInstance;
        setScope(particleEffectInstance.scopePayload); //Default set to the parent payload instance
        init();
	}

	public void init () {
		position.set(0, 0);

		emitterModule = emitterGraph.getEmitterModule();
		if (emitterModule == null)
			return;

		delay = emitterModule.getDelay();
		duration = emitterModule.getDuration();
		isContinuous = emitterModule.isContinuous();

		delayTimer = delay;


		// inner variable defaults
		alpha = 0f;
		isComplete = false;
		particlesToEmmit = 1f; // always emmit one first

		initialized = true;
	}

	public void update (float delta) {
		emitterModule = emitterGraph.getEmitterModule();
		if (emitterModule == null)
			return;

		if(!initialized) {
			init();
		}

		if(paused) return;

		//update variables to their real values
		emitterModule.updateScopeData(this);

		delay = emitterModule.getDelay();
		duration = emitterModule.getDuration();
		isContinuous = emitterModule.isContinuous();
		rate = emitterModule.getRate();
		isAttached = emitterModule.isAttached();
		isAdditive = emitterModule.isAdditive();

		if(delayTimer > 0) {
			delayTimer -= delta;
			if(delayTimer < 0) delayTimer = 0;
			if(delayTimer > 0) {

				updateParticles(delta); // process existing particles at least

				emitterGraph.resetRequesters();
				return;
			}
		}

		float normDelta = delta/duration;

		float deltaLeftover = 0;
		if(alpha + normDelta > 1f) {
			deltaLeftover = (1f - alpha) * duration;
			alpha = 1f;
		} else {
			alpha += normDelta;
			deltaLeftover = delta;
		}

		//update variables to their real values
		emitterModule.updateScopeData(this);

		//
		if (alpha < 1f || (alpha == 1f && deltaLeftover > 0)) { // emission only here
			// let's emmit
			particlesToEmmit += rate * deltaLeftover;

			int count = (int)particlesToEmmit;
			for (int i = 0; i < count; i++) {
				Particle particle = particlePool.obtain();
				if (emitterGraph.getParticleModule() != null) {
					particle.init(emitterGraph.getParticleModule(), this);
					activeParticles.add(particle);
				}
			}
			particlesToEmmit -= count;
		}

		// process existing particles.
		updateParticles(delta);

		if (alpha == 1f) {
			if (isContinuous && !isStopped) {
				// let's repeat
				restart();
			}
		}

		if(activeParticles.size == 0) {
			isComplete = true;
		} else {
			isComplete = false;
		}

		emitterGraph.resetRequesters();
	}

	private void updateParticles(float delta) {
		for (int i = activeParticles.size - 1; i >= 0; i--) {
			Particle particle = activeParticles.get(i);
			particle.update(delta);
			if (particle.alpha >= 1f) {
				particlePool.free(particle);
				activeParticles.removeIndex(i);
			}
		}
	}

	public void restart() {
    	delayTimer = delay;
    	alpha = 0;
    	isComplete = false;
		particlesToEmmit = 1f;
        isStopped = false;
	}

    public void setScope (ScopePayload scope) {
        emitterGraph.setScope(scope);
    }

    public ParticleEffectInstance getEffect() {
    	return parentParticleInstance;
	}

	public boolean isAttached() {
		return isAttached;
	}

	public void stop() {
		alpha = 1f;
        isStopped = true;
	}

	public void pause() {
		paused = true;
	}

	public void resume() {
		paused = false;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public void setTint(float r, float g, float b, float a) {
		tint.set(r, g, b, a);
	}

	public void setTint(Color color) {
    	tint.set(color);
	}
}
