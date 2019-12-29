package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.utils.unsafe.jsObject

object Harvester : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.HARVESTER }
    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        //TODO make dynamic
        return arrayOf(WORK, CARRY, MOVE)
    }
}

fun Creep.harvest(fromRoom: Room = this.room, toRoom: Room = this.room) {
    if ((carry.energy < carryCapacity) || (memory.role == Role.MINER)) {
        val source = pos.findClosestByPath(FIND_DROPPED_RESOURCES) ?: pos.findClosestByPath(FIND_SOURCES)
        if (source is Resource) {
            pickup(source)
        }
        if (source != null) {
            moveTo(source as NavigationTarget)
        }
    } else {
        moveToAndTransferEnergy(findNearestEnergyStructure())
    }
}
