package com.boxtrotstudio.ghost.client.network.packets

import box2dLight.ConeLight
import box2dLight.PointLight
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class SpawnLightPacket : Packet<PlayerClient>(){
    override fun handle(){
        var id = consume(2).asShort()

        var x = consume(4).asFloat()
        var y = consume(4).asFloat()
        var radius = consume(4).asFloat()
        var intensity = consume(4).asFloat()

        var color = consume(4).asInt()

        val isConeLight = consume(1).asBoolean()

        var directionDegrees = 90f
        var coneDegrees = 30f

        if (isConeLight) {
            directionDegrees = consume(4).asFloat()
            coneDegrees = consume(4).asFloat()
        }

        Gdx.app.postRunnable {
            val c = Color(color)
            System.out.println(c);
            System.out.println("" + c.r + " : " + c.g + " : " + c.b + " : " + intensity)

            Gdx.app.postRunnable {
                if (!isConeLight)
                    PointLight(Ghost.rayHandler, 128, c, radius, x, y)
                else
                    ConeLight(Ghost.rayHandler, 128, c, radius, x, y, directionDegrees, coneDegrees)
            }
        }
    }
}

