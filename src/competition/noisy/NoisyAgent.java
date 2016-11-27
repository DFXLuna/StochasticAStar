package competition.noisy;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.Agent;
import competition.noisy.AStarSimulator.*;
import java.util.ArrayList;
/**
 * User: Matt Grant 
 * Date: 11/5/2016
 * Package: competition.noisy
 */
// Some people write disgusting code. We call those people Java Devs
// I thought python was atrocious but Java is clearly on another level
// Have you ever looked at the interface for 
// implementing a Priority queue in Java? It's disgusting


public class NoisyAgent implements Agent{

	protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "NoisyAgent";
    private AStarSimulator sim;
    private ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    public boolean requirePlan = true;
    private boolean[] prevAction = null;
    
    public void reset(){
        requirePlan = true;
        action = new boolean[Environment.numberOfButtons];
        sim = new AStarSimulator();
    }

	// TODO
    // Simulation becomes inaccurate when we take more than some amount of time to plan
    // Add noise
    // Deal with noise
    public boolean[] getAction(Environment observation){
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
    	if(prevAction != null){
    		sim.advanceStep(prevAction);
    	}
    	sim.setLevelPart(scene, enemies);
    	
    	float[] m = observation.getMarioFloatPos();
    	if (sim.levelScene.mario.x != m[0] && sim.levelScene.mario.y != m[1]){
    		System.out.println("Inaccuracy in simulator position");
    		sim.levelScene.mario.x = m[0];
    		sim.levelScene.mario.y = m[0];
        	sim.setLevelPart(scene, enemies);
    		requirePlan = true;

    	}
    	// We only want to replan if the last plan has expired
    	if(requirePlan){
    		plan();
    		requirePlan = false;
    	}
        return popAction();
    }
    private void plan(){
    	plan.clear();
    	plan.addAll(sim.getPlan());
    	sim.simNull();
    }
    
    public boolean[] popAction(){
    	if(plan.size() == 1){
    		requirePlan = true;
    	}
		boolean[] toReturn = plan.get(0);
		prevAction = toReturn;
		plan.remove(0);
		return toReturn;
    }
    
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {        
    	return name;
    }

    public void setName(String Name) {
    	this.name = Name;    
    }
    

}
