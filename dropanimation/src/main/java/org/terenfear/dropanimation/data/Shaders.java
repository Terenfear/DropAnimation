package org.terenfear.dropanimation.data;

import org.intellij.lang.annotations.Language;

/**
 * Created with IntlliJ IDEA<br>
 * User: Pavel Kozlovich <br>
 * E-mail: terenfear@gmail.com<br>
 * Skype: terenfear962<br>
 * Date  21.12.2017<br>
 * Time: 12:07<br>
 * Project name: TestDropAnimation<br>
 * ======================================================================================================================
 */
public class Shaders {

    public static final float TEXTURE_COORDS[] = {
            0, 1,
            1, 1,
            0, 0,
            1, 0,
    };

    public static final String ATTR_MVP_MATRIX = "uMVPMatrix";
    public static final String ATTR_POSITION = "vPosition";
    public static final String ATTR_TEXTURE_COORDINATE = "vTextureCoordinate";

    @Language("GLSL")
    public static final String VERTEX_SHADER = ""+
            "precision mediump float;" +
            "uniform mat4 " + ATTR_MVP_MATRIX + ";" +
            "attribute vec4 " + ATTR_POSITION + ";" +
            "attribute vec4 " + ATTR_TEXTURE_COORDINATE + ";" +
            "varying vec2 position;" +
            "void main(){" +
            " gl_Position = " + ATTR_MVP_MATRIX + " * " + ATTR_POSITION + ";" +
            " position = " + ATTR_TEXTURE_COORDINATE + ".xy;" +
            "}";
    @Language("GLSL")
    public static final String FRAGMENT_SHADER = ""+
            "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 position;" +
            "void main() {" +
            "    gl_FragColor = texture2D(uTexture, position);" +
            "}";
}
