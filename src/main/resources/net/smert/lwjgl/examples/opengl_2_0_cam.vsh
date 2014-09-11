#version 120

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main(void)
{
    gl_FrontColor = gl_Color;
    gl_TexCoord[0] = gl_MultiTexCoord0;

    gl_Position = uProjection * uView * uModel * gl_Vertex;
}