package io.r2n.screeps.ai


import io.r2n.screeps.ai.roles.*
import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete
import screeps.utils.unsafe.jsObject

fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    //make sure we have at least some creeps
    spawnCreeps(Game.creeps.values, mainSpawn)

    // build a few extensions so we can have 550 energy
    val controller = mainSpawn.room.controller
    if (controller != null && controller.level >= 2) {
        when (controller.room.find(FIND_MY_STRUCTURES).count { it.structureType == STRUCTURE_EXTENSION }) {
            0 -> controller.room.createConstructionSite(29, 27, STRUCTURE_EXTENSION)
            1 -> controller.room.createConstructionSite(28, 27, STRUCTURE_EXTENSION)
            2 -> controller.room.createConstructionSite(27, 27, STRUCTURE_EXTENSION)
            3 -> controller.room.createConstructionSite(26, 27, STRUCTURE_EXTENSION)
            4 -> controller.room.createConstructionSite(25, 27, STRUCTURE_EXTENSION)
            5 -> controller.room.createConstructionSite(24, 27, STRUCTURE_EXTENSION)
            6 -> controller.room.createConstructionSite(23, 27, STRUCTURE_EXTENSION)
        }
    }

    //spawn a big creep if we have plenty of energy
    for ((_, room) in Game.rooms) {
        if (room.energyAvailable >= 550) {
            mainSpawn.spawnCreep(
                    arrayOf(
                            WORK,
                            WORK,
                            WORK,
                            WORK,
                            CARRY,
                            MOVE,
                            MOVE
                    ),
                    "HarvesterBig_${Game.time}",
                    options {
                        memory = jsObject<CreepMemory> {
                            this.role = Role.HARVESTER
                        }
                    }
            )
            console.log("hurray!")
        }
    }

    for ((_, creep) in Game.creeps) {
        when (creep.memory.role) {
            Role.HARVESTER -> creep.harvest()
            Role.BUILDER -> creep.build()
            Role.UPGRADER -> creep.upgrade(mainSpawn.room.controller!!)
            else -> creep.pause()
        }
    }

}


fun thereAreMoreHarvestersThanBuilders(creeps: Array<Creep>, delta: Int): Boolean {
    return creeps.count { it.memory.role == Role.HARVESTER } >
            creeps.count() { it.memory.role == Role.BUILDER } + delta
}

fun thereAreMoreBuildersThanHarvesters(creeps: Array<Creep>, delta: Int = 0): Boolean {
    return !thereAreMoreBuildersThanHarvesters(creeps, delta)
}

private fun spawnCreeps(
        creeps: Array<Creep>,
        spawn: StructureSpawn
) {

    val body = arrayOf<BodyPartConstant>(WORK, CARRY, MOVE)
    val maxHarvesters = 4
    val maxUpgraders = 3

    if (spawn.room.energyAvailable < body.sumBy { BODYPART_COST[it]!! }) {
        return
    }

    val role: Role = when {
        creeps.count {it.memory.role == Role.HARVESTER} <= maxHarvesters -> Role.HARVESTER

        creeps.count { it.memory.role == Role.UPGRADER } <= maxUpgraders -> Role.UPGRADER

        spawn.room.find(FIND_MY_CONSTRUCTION_SITES).isNotEmpty() &&
                thereAreMoreHarvestersThanBuilders(creeps, delta = 1) -> Role.BUILDER

        else -> return
    }

    val newName = "${role.name}_${Game.time}"
    val code = spawn.spawnCreep(body, newName, options {
        memory = jsObject<CreepMemory> { this.role = role }
    })

    when (code) {
        OK -> console.log("spawning $newName with body $body")
        ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run { } // do nothing
        else -> console.log("unhandled error code $code")
    }
}

private fun houseKeeping(creeps: Record<String, Creep>) {
    if (Game.creeps.isEmpty()) return  // this is needed because Memory.creeps is undefined

    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {
            console.log("deleting obsolete memory entry for creep $creepName")
            delete(Memory.creeps[creepName])
        }
    }
}
