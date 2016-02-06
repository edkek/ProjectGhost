package me.eddiep.ghost.client.core.render.scene.impl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import me.eddiep.ghost.client.core.render.scene.AbstractScene
import me.eddiep.ghost.client.core.render.scene.Scene

class BlurredScene(val original: Scene) : AbstractScene() {
    private lateinit var targetA: FrameBuffer
    private lateinit var targetB: FrameBuffer
    private lateinit var regionA: TextureRegion
    private lateinit var regionB: TextureRegion
    private lateinit var shader: ShaderProgram
    override fun init() {
        targetA = FrameBuffer(Pixmap.Format.RGBA8888, original.width, original.height, false)
        regionA = TextureRegion(targetA.colorBufferTexture, targetA.width, targetA.height)
        regionA.flip(false, true) //Flip the y-axis

        targetB = FrameBuffer(Pixmap.Format.RGBA8888, original.width, original.height, false)
        regionB = TextureRegion(targetB.colorBufferTexture, targetB.width, targetB.height)
        regionB.flip(false, true) //Flip the y-axis

        targetA.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        targetB.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        original.init()

        shader = ShaderProgram(Gdx.files.local("shaders/blur.vert"), Gdx.files.local("shaders/blur.frag"))

        if (shader.log.length !=0)
            System.out.println(shader.log);

        shader.begin()
        shader.setUniformf("dir", 0f, 0f)
        shader.setUniformf("resolution", original.width.toFloat())
        shader.setUniformf("radius", 5f)
        shader.end()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        targetA.begin()

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        original.render(camera, batch)

        targetA.end()

        batch.shader = shader

        targetB.begin()

        batch.begin()

        shader.setUniformf("dir", 1f, 0f)
        shader.setUniformf("resolution", original.width.toFloat())

        batch.draw(regionA, 0f, 0f)

        batch.flush()
        //batch.end()

        targetB.end()

        shader.setUniformf("dir", 0f, 1f)
        shader.setUniformf("resolution", original.height.toFloat())
        //batch.begin()

        batch.draw(regionB, 0f, 0f)
        batch.flush()
        batch.end()

        if (original is SpriteScene) {
            original.rayHandler.updateAndRender()
        }

        batch.shader = null
    }

    override fun dispose() {
        targetA.dispose()
        original.dispose()
    }

    override fun isVisible() : Boolean {
        return original.isVisible
    }
}
