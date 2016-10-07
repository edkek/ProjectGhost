package shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DynamicShadowShader {
	static final public ShaderProgram createShadowShader(int colorReduction) {
		final String vertexShader = "attribute vec4 a_position;\n" //
				+ "attribute vec2 a_texCoord;\n" //
				+ "varying vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   v_texCoords = a_texCoord;\n" //
				+ "   gl_Position = a_position;\n" //
				+ "}\n";

		// this is allways perfect precision
		final String fragmentShader = "#ifdef GL_ES\n" //
				+ "precision lowp float;\n" //
				+ "#define MED mediump\n"				
				+ "#else\n"				
				+ "#define MED \n"
				+ "#endif\n" //
				+ "varying MED vec2 v_texCoords;\n" //
				+ "uniform sampler2D u_texture;\n" //
				+ "uniform sampler2D u_shadows;\n" //
				+ "uniform vec4 ambient;\n"
				+ "void main()\n"//
				+ "{\n" //
				+ "    vec4 c = texture2D(u_texture, v_texCoords);\n"
				+ "    gl_FragColor.rgb = (ambient.rgb + " + colorReduction + ".0 * c.rgb - texture2D(u_shadows, v_texCoords).rgb);\n"
				+ "    gl_FragColor.a = ambient.a - c.a;"
				+ "}\n";
		ShaderProgram.pedantic = false;
		ShaderProgram shadowShader = new ShaderProgram(vertexShader,
					fragmentShader);
		if (shadowShader.isCompiled() == false) {
			Gdx.app.log("ERROR", shadowShader.getLog());

		}

		return shadowShader;
	}

}
