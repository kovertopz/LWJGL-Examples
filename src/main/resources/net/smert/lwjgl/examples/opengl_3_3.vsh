#version 330

layout(location = 0) in vec4 in_Color;
layout(location = 1) in vec3 in_Position;
layout(location = 2) in vec2 in_TexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

out vec4 pass_Color;
out vec2 pass_TexCoord;

void main(void)
{
    pass_Color = in_Color;
    pass_TexCoord = in_TexCoord;

    gl_Position = uProjection * uView * uModel * vec4(in_Position, 1.0);
}