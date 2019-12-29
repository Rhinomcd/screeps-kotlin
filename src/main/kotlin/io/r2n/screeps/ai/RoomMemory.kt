package io.r2n.screeps.ai

import screeps.api.GlobalMemory
import screeps.api.RoomMemory
import screeps.api.Source
import screeps.utils.memory.memory


var RoomMemory.sources: Array<Source> by memory { emptyArray<Source>() }
var RoomMemory.lastMinerAssignment by memory { 0 }
