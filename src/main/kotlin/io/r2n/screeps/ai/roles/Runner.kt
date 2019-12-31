package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.Structure
import screeps.utils.unsafe.jsObject

object Runner : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.RUNNER }
    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        var body = emptyList<BodyPartConstant>()
        var usedEnergy = 0
        loop@ while (usedEnergy <= maxEnergy) {
            when {
                thereAreLessCarryThanMove(body) && addingPartWontExceedMax(CARRY, usedEnergy, maxEnergy) -> {
                    body += CARRY
                    usedEnergy += BODYPART_COST[CARRY]!!
                }
                addingPartWontExceedMax(MOVE, usedEnergy, maxEnergy) -> {
                    body += MOVE
                    usedEnergy += BODYPART_COST[MOVE]!!
                }
                else -> {
                    break@loop
                }
            }
        }
        return body.toTypedArray()
    }


}

private fun thereAreLessCarryThanMove(bodyParts: List<BodyPartConstant>, delta: Int = 0): Boolean {
    return bodyParts.count { it == MOVE } > bodyParts.count { it == CARRY } + delta - 1
}

fun Creep.run() {
    if (carry.energy < carryCapacity && !memory.working) {
        val energy = room.find(FIND_DROPPED_RESOURCES)
                .filter { it.resourceType == RESOURCE_ENERGY }
                .maxBy { it.amount }
        if (energy != null && pickup(energy) == ERR_NOT_IN_RANGE) {
            moveTo(energy.pos)
        }
    } else {
        memory.working = true
        val target: Structure? = findEnergyStructures()
                .filter { it.energy < it.energyCapacity }
                .mapNotNull { it as? Structure }
                .minBy { pos.getRangeTo(it) }
        moveToAndTransferEnergy(target)
    }
}