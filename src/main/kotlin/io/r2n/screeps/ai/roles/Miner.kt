package io.r2n.screeps.ai.roles

import io.r2n.screeps.ai.getBodyCost
import io.r2n.screeps.ai.lastMinerAssignment
import io.r2n.screeps.ai.sources
import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.utils.memory.memory
import screeps.utils.unsafe.jsObject

var CreepMemory.assignedSourcePosition: RoomPosition? by memory()

object Miner : EmployedCreep {
    private val bodyRatio = mapOf(
            WORK to 2,
            MOVE to 1
    )
    private const val MAX_WORK = 5;
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> {
            role = Role.MINER
        }
    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        var body = emptyList<BodyPartConstant>()
        var usedEnergy = 0
        loop@ while (usedEnergy <= maxEnergy) {
            when {
                thereAreLessWorkThanMoveParts(body, bodyRatio[WORK] ?: 0) &&
                        body.count { it == WORK } <= MAX_WORK
                        && addingWORKWontExceedMax(usedEnergy, maxEnergy) -> {
                    body += WORK
                    usedEnergy += BODYPART_COST[WORK]!!
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

    private fun addingWORKWontExceedMax(usedEnergy: Int, maxEnergy: Int): Boolean {
        return usedEnergy + getBodyCost(arrayOf(WORK)) <= maxEnergy
    }


    private fun thereAreLessWorkThanMoveParts(bodyParts: List<BodyPartConstant>, delta: Int = 0): Boolean {
        return bodyParts.count { it == WORK } < bodyParts.count { it == MOVE } + delta - 1
    }
}

fun Creep.getAssignedSourcePosition() {
    memory.assignedSourcePosition = room.memory.sources[room.memory.lastMinerAssignment].pos
}

fun Creep.debugAssignedSources() {
    if (memory.assignedSourcePosition != null) {
        room.visual.text("F", memory.assignedSourcePosition!!)
    }
}

fun Creep.mine() {
    getAssignedSourcePosition()
    debugAssignedSources()
    if (memory.assignedSourcePosition != null) {
        val target: Source? = room.lookForAt(LOOK_SOURCES,
                memory.assignedSourcePosition!!.x,
                memory.assignedSourcePosition!!.y)?.firstOrNull()
        if (target == null) {
            console.log("ERROR - worker assigned an unidentifiable source [worker=${name}," +
                    " sourceLocation=${memory.assignedSourcePosition!!.x}, ${memory.assignedSourcePosition!!.y}")

        } else if (harvest(target) == ERR_NOT_IN_RANGE) {
            moveTo(target.pos)
        }
    }
}
