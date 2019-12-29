package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.Structure
import screeps.utils.memory.memory
import screeps.utils.unsafe.jsObject

var CreepMemory.assignedBuildingSite: ConstructionSite? by memory()

object Builder : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.BUILDER }
    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        //TODO Implement this
        return arrayOf(WORK, CARRY, MOVE)
    }

}

fun Creep.build(assignedRoom: Room = this.room) {
    if (memory.working && carry.energy == 0) {
        memory.working = false
        say("🔄 harvest")
    }
    if (!memory.working && carry.energy == carryCapacity) {
        memory.working = true
        say("🚧 build")
    }

    if (memory.assignedBuildingSite != null) {
        if (memory.working) {
            when (val buildStatusCode = build(memory.assignedBuildingSite!!)) {
                ERR_NOT_IN_RANGE -> {
                    val code = moveTo(memory.assignedBuildingSite!!)
                }
                ERR_INVALID_TARGET -> {
                    memory.assignedBuildingSite = null
                }
                else -> {
                    console.log("builder error code: $buildStatusCode")
                }
            }
        } else {
            moveToAndWithdrawEnergy(findNearestEnergyStructure())
        }
    } else {
        memory.assignedBuildingSite = pos.findClosestByPath(FIND_CONSTRUCTION_SITES)
        if (memory.assignedBuildingSite == null) {
            say("Greetings, snackbar")
            suicide()
        }
    }
}
