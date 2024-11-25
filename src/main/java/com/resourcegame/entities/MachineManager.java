package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineType;
import com.resourcegame.core.GameMap;
import java.util.List;
import java.util.ArrayList;

public class MachineManager {
    private List<Machine> machines;
    private GameMap gameMap;
    
    public MachineManager(GameMap gameMap) {
        this.machines = new ArrayList<>();
        this.gameMap = gameMap;
    }
    
    public Machine createMachine(MachineType type, Position position) {
        Machine machine = null;
        if (type.toString().contains("HARVESTER")) {
            machine = new Harvester(position, type);
        } else if (type.toString().contains("FACTORY")) {
            machine = new Factory(position, type);
        }
        
        if (machine != null) {
            machines.add(machine);
        }
        return machine;
    }
    
    public void updateMachines() {
        for (Machine machine : machines) {
            machine.update(gameMap);
        }
    }
    
    public Machine getMachineAt(Position position) {
        return machines.stream()
            .filter(m -> m.getPosition().equals(position))
            .findFirst()
            .orElse(null);
    }
    
    public void removeMachine(Position position) {
        machines.removeIf(m -> m.getPosition().equals(position));
    }
    
    public List<Machine> getAllMachines() {
        return new ArrayList<>(machines);
    }
}