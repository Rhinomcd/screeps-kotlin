package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.Structure
import screeps.api.structures.StructureController
import screeps.utils.unsafe.jsObject

object Upgrader : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.UPGRADER }

    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        var body = emptyList<BodyPartConstant>()
        var usedEnergy = 0
        sequenceOf(WORK, MOVE, CARRY, CARRY, WORK).takeWhile { usedEnergy <= maxEnergy }.forEach {
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

fun Creep.upgrade(controller: StructureController) {
    if (carry.energy < carryCapacity && !memory.working) {
        val energySource = findEnergyStructures()
                .filter { it.energy > 0 }
                .map { it as Structure }
                .minBy { pos.getRangeTo(it) }
        moveToAndWithdrawEnergy(energySource)
    } else {
        memory.working = true
        if (upgradeController(controller) == ERR_NOT_IN_RANGE) {
            moveTo(controller.pos)
        } else if (upgradeController(controller) == ERR_NOT_ENOUGH_ENERGY) {
            memory.working = false
        }
    }
}

