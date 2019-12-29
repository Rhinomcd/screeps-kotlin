package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.utils.memory.memory
import screeps.utils.unsafe.jsObject

var CreepMemory.assignedBuildingSiteId: String? by memory()

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

    if (memory.assignedBuildingSiteId != null) {
        val target: ConstructionSite? = Game.getObjectById<ConstructionSite>(memory.assignedBuildingSiteId)
        if (memory.working && target != null) {
            when (val buildStatusCode = build(target)) {
                ERR_NOT_IN_RANGE -> {
                    val code = moveTo(target)
                }
                ERR_INVALID_TARGET -> {
                    memory.assignedBuildingSiteId = null
                }
                OK -> run { }
                else -> {
                    console.log("builder error code: $buildStatusCode")
                }
            }
        } else {
            moveToAndWithdrawEnergy(findNearestEnergyStructure())
        }
    } else {
        memory.assignedBuildingSiteId = pos.findClosestByPath(FIND_MY_CONSTRUCTION_SITES)?.id.orEmpty()
        if (memory.assignedBuildingSiteId.isNullOrBlank()) {
            say("Greetings, snackbar")
            suicide()
        }
    }
}
