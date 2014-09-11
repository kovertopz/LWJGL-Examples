#version 330

uniform float uTextureFlag = 0.0;
uniform sampler2D uTexture0;

in vec4 pass_Color;
in vec2 pass_TexCoord;

out vec4 out_Color;

void main(void)
{
    vec4 textureColor = texture2D(uTexture0, pass_TexCoord);

    out_Color = mix(pass_Color, textureColor * pass_Color, uTextureFlag);
}