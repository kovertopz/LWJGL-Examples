#version 120

uniform float uTextureFlag = 0.0;
uniform sampler2D uTexture0;

void main(void)
{
    vec4 textureColor = texture2D(uTexture0, gl_TexCoord[0].st);

    gl_FragColor = mix(gl_Color, textureColor * gl_Color, uTextureFlag);
}