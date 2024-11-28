package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineStatus;
import com.resourcegame.utils.MachineType;
import com.resourcegame.core.GameMap;
import java.util.Random;

public abstract class Machine {
    protected Position position;
    protected Inventory inventory;
    protected boolean isWorking;
    protected MachineType type;
    protected final int inventoryCapacity;
    protected long lastProcessTime;
    protected int processingSpeed;
    protected MachineStatus status;
    protected boolean needsMaintenance;
    protected int timesConfigured;
    protected int operationsSinceMaintenance;
    public static final int FRAGILE_MAINTENANCE_THRESHOLD = 3;
    public static final int NORMAL_MAINTENANCE_THRESHOLD = 10;// Original threshold for regular machines
    protected static final Random random = new Random();
    private static final double MAX_BREAKDOWN_MULTIPLIER = 3.0;
    private static final double WARNING_THRESHOLD_PERCENTAGE = 0.7;
    private static final long OPERATION_COOLDOWN = 1000;
    protected long lastOperationTime;

    public Machine(Position position, MachineType type) {
        this.position = position;
        this.type = type;
        this.isWorking = false;
        this.lastProcessTime = System.currentTimeMillis();
        this.needsMaintenance = false;
        this.timesConfigured = 0;
        this.operationsSinceMaintenance = 0;

        // Set capacity and speed based on machine type
        if (type.toString().contains("ADVANCED")) {
            this.inventoryCapacity = 200;
            this.processingSpeed = 2; // 2x faster
        } else {
            this.inventoryCapacity = 100;
            this.processingSpeed = 1;
        }

        this.status = MachineStatus.IDLE;
        this.inventory = new Inventory(this.inventoryCapacity);
    }

    public abstract void update(GameMap gameMap);

    protected boolean checkMaintenance() {
        if (needsMaintenance) {
            setStatus(MachineStatus.NEEDS_MAINTENANCE);
            return false;
        }

        // Don't check for breakdowns unless we've done some operations
        if (operationsSinceMaintenance == 0) {
            return true;
        }

        int threshold = type.isFragile() ? FRAGILE_MAINTENANCE_THRESHOLD : NORMAL_MAINTENANCE_THRESHOLD;

        if (operationsSinceMaintenance >= threshold) {
            // Gentler scaling - logarithmic instead of linear
            float breakdownChance = type.isFragile()
                    ? type.getBreakdownChance()
                            * (float) (1.0 + Math.log10(1 + (operationsSinceMaintenance - threshold) * 0.1))
                    : type.getBreakdownChance();

            // Cap the maximum chance
            breakdownChance = Math.min(breakdownChance, 0.5f);

            if (random.nextFloat() < breakdownChance) {
                needsMaintenance = true;
                setStatus(MachineStatus.NEEDS_MAINTENANCE);
                return false;
            }
        }
        return true;
    }

    public double getEfficiencyMultiplier() {
        int threshold = type.isFragile() ? FRAGILE_MAINTENANCE_THRESHOLD : NORMAL_MAINTENANCE_THRESHOLD;
        if (operationsSinceMaintenance >= threshold * WARNING_THRESHOLD_PERCENTAGE) {
            return 1.0 - ((double) (operationsSinceMaintenance - threshold * WARNING_THRESHOLD_PERCENTAGE)
                    / threshold) * 0.3; // Max 30% efficiency loss
        }
        return 1.0;
    }

    public boolean performMaintenance() {
        if (needsMaintenance) {
            needsMaintenance = false;
            operationsSinceMaintenance = 0;
            setStatus(MachineStatus.IDLE);
            return true;
        }
        return false;
    }

    public boolean canBeReconfigured() {
        return type.getConfigurationLimit() == 0 || timesConfigured < type.getConfigurationLimit();
    }

    protected void incrementConfigurationCount() {
        if (type.getConfigurationLimit() > 0) {
            timesConfigured++;
        }
    }

    public int getRemainingConfigurations() {
        if (type.getConfigurationLimit() == 0) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, type.getConfigurationLimit() - timesConfigured);
    }

    public Position getPosition() {
        return position;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public MachineType getType() {
        return type;
    }

    public MachineStatus getStatus() {
        return status;
    }

    protected void setStatus(MachineStatus newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;
            isWorking = (newStatus == MachineStatus.WORKING);
        }
    }

    public int getInventoryCapacity() {
        return inventoryCapacity;
    }

    public abstract String getStatusMessage();

    public boolean needsMaintenance() {
        return needsMaintenance;
    }

    public int getOperationsSinceMaintenance() {
        return operationsSinceMaintenance;
    }

    public static int getMaintenanceThreshold(MachineType type) {
        return type.isFragile() ? FRAGILE_MAINTENANCE_THRESHOLD : NORMAL_MAINTENANCE_THRESHOLD;
    }

    public int getMaintenanceThreshold() {
        return type.isFragile() ? FRAGILE_MAINTENANCE_THRESHOLD : NORMAL_MAINTENANCE_THRESHOLD;
    }

    public static int getFragileMaintenanceThreshold() {
        return FRAGILE_MAINTENANCE_THRESHOLD;
    }

    protected void incrementOperations() {
        long currentTime = System.currentTimeMillis();
        // Only increment if enough time has passed since last operation
        if (currentTime - lastOperationTime >= OPERATION_COOLDOWN) {
            operationsSinceMaintenance++;
            lastOperationTime = currentTime;
        }
    }

    protected boolean canOperate() {
        return !needsMaintenance;
    }

}