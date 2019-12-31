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
        var body = emptyList<BodyPartConstant>()
        var usedEnergy = 0
        sequenceOf(WORK, CARRY, MOVE).takeWhile { usedEnergy <= maxEnergy }.forEach {
            when {
                addingPartWontExceedMax(it, usedEnergy, maxEnergy) -> {
                    body += it
                    usedEnergy += BODYPART_COST[it]!!
                }
            }
        }
        return body.toTypedArray()
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
        val target: ConstructionSite? = Game.getObjectById(memory.assignedBuildingSiteId)
        say("TARGET: ${target}")
        if (memory.working && target != null) {
            when (val buildStatusCode = build(target)) {
                ERR_NOT_IN_RANGE -> {
                    val code = moveTo(target)
                    say("RANGE")
                }
                ERR_INVALID_TARGET -> {
                    memory.assignedBuildingSiteId = null
                    say("TARGET")
                }
                OK -> say("OK")
                else -> {
                    say("NOT OK")
                    console.log("builder error code: $buildStatusCode")
                }
            }
        } else if (!memory.working) {
            moveToAndWithdrawEnergy(findNearestEnergyStructureWithEnergy())
            say("GET N R G")
        }
    } else {
        say("ELSE")
        memory.assignedBuildingSiteId = pos.findClosestByPath(FIND_MY_CONSTRUCTION_SITES)?.id.orEmpty()
        if (memory.assignedBuildingSiteId.isNullOrBlank()) {
            say("Greetings, snackbar")
            suicide()
        }
    }
}
