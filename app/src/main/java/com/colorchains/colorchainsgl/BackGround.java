package com.colorchains.colorchainsgl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by francisco.cnmarao on 05/05/2017.
 */

public class BackGround extends Entity {
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    /* SHADER Image
 *
 * This shader is for rendering 2D images straight from a texture
 * No additional effects.
 *
 */
    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fs_Image =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform float color;" +
                    "uniform vec2 resolution;" +
                    "uniform float time;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );\n" +
                    "}";
    public static final String fs_Image_effect =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform vec4 color;" +
                    "uniform vec2 resolution;" +
                    "uniform float time;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(color.r,color.g,color.b,color.a);\n" +
                    "}";
    public static final String effect =
            "precision mediump float;\n" +
                    "uniform float time;\n" +
                    "uniform vec2 resolution;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_texture;\n" +
                    "\n" +
                    "float windows(vec2 uv){\n" +
                    "  uv.x += sin(uv.y * 12.0 + time) * 0.02;\n" +
                    "  uv.y -= sin(uv.x * 12.0 + time) * 0.02;\n" +
                    "  float box = smoothstep(0.55, 0.604, uv.x) * (1.0 - smoothstep(1.196, 1.25, uv.x)) * smoothstep(0.25, 0.304, uv.y) * (1.0 - smoothstep(0.796, 0.85, uv.y));\n" +
                    "  box *= smoothstep(0.005, 0.008, abs(uv.x - 0.9));\n" +
                    "  box *= smoothstep(0.005, 0.008, abs(uv.y - 0.55));\n" +
                    "  return box;\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\n" +
                    " vec2 position = ( gl_FragCoord.xy / resolution.xy ) ;\n" +
                    " position.x *= resolution.x / resolution.y;\n" +
                    " gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(vec3(0., windows(position), 0.), 1.);\n" +
                    " //gl_FragColor = vec4(0.5,0,0,1);\n" +
                    " //gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(0.5,0,0,1) * position;\n" +
                    "\n" +
                    "}";

    public static final String effect3 =
            "precision mediump float;\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "uniform vec2 resolution;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_texture;\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "  vec2 st = (gl_FragCoord.xy * 2.0 - resolution) / min(resolution.x, resolution.y);\n" +
                    "  // How to move spher is here (https://goo.gl/MolkFJ).\n" +
                    "  st.x += cos(time);\n" +
                    "  st.y += sin(time);\n" +
                    "  // See this page (https://goo.gl/KJ9ScK) about the formula below.\n" +
                    "  float f = abs(sin(time)) * 0.1 / length(st);\n" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(vec3(f), 1.0);\n" +
                    "}";
    public static final String effect2 =
            "  // this is the resolution of the window\n" +
                    "  uniform vec2 resolution;\n" +
                    "  // this is a count in seconds.\n" +
                    "  uniform float time;\n" +
                    "  void main() {\n" +
                    "      // gl_FragCoord is the position of the pixel being drawn\n" +
                    "      // so this code makes p a value that goes from -1 to +1 \n" +
                    "      // x and y\n" +
                    "      vec2 p = -1.0 + 2.0 * gl_FragCoord.xy / resolution.xy;\n" +
                    "      // a = the time speed up by 40\n" +
                    "      float a = time*40.0;\n" +
                    "      // declare a bunch of variables.\n" +
                    "      float d,e,f,g=1.0/40.0,h,i,r,q;\n" +
                    "      // e goes from 0 to 400 across the screen\n" +
                    "      e=400.0*(p.x*0.5+0.5);\n" +
                    "      // f goes from 0 to 400 down the screen\n" +
                    "      f=400.0*(p.y*0.5+0.5);\n" +
                    "      // i goes from 200 + or - 20 based\n" +
                    "      // on the sin of e * 1/40th + the slowed down time / 150\n" +
                    "      // or in other words slow down even more.\n" +
                    "      // e * 1/40 means e goes from 0 to 1\n" +
                    "      i=200.0+sin(e*g+a/150.0)*20.0;\n" +
                    "      // d is 200 + or - 18.0 + or - 7\n" +
                    "      // the first +/- is cos of 0.0 to 0.5 down the screen\n" +
                    "      // the second +/i is cos of 0.0 to 1.0 across the screen\n" +
                    "      d=200.0+cos(f*g/2.0)*18.0+cos(e*g)*7.0;\n" +
                    "      // I'm stopping here. You can probably figure out the rest\n" +
                    "      // see answer\n" +
                    "      r=sqrt(pow(i-e,2.0)+pow(d-f,2.0));\n" +
                    "      q=f/r;\n" +
                    "      e=(r*cos(q))-a/2.0;f=(r*sin(q))-a/2.0;\n" +
                    "      d=sin(e*g)*176.0+sin(e*g)*164.0+r;\n" +
                    "      h=((f+d)+a/2.0)*g;\n" +
                    "      i=cos(h+r*p.x/1.3)*(e+e+a)+cos(q*g*6.0)*(r+h/3.0);\n" +
                    "      h=sin(f*g)*144.0-sin(e*g)*212.0*p.x;\n" +
                    "      h=(h+(f-e)*q+sin(r-(a+h)/7.0)*10.0+i/4.0)*g;\n" +
                    "      i+=cos(h*2.3*sin(a/350.0-q))*184.0*sin(q-(r*4.3+a/12.0)*g)+tan(r*g+h)*184.0*cos(r*g+h);\n" +
                    "      i=mod(i/5.6,256.0)/64.0;\n" +
                    "      if(i<0.0) i+=4.0;\n" +
                    "      if(i>=2.0) i=4.0-i;\n" +
                    "      d=r/350.0;\n" +
                    "      d+=sin(d*d*8.0)*0.52;\n" +
                    "      f=(sin(a*g)+1.0)/2.0;\n" +
                    "      gl_FragColor=vec4(vec3(f*i/1.6,i/2.0+d/13.0,i)*d*p.x+vec3(i/1.3+d/8.0,i/2.0+d/18.0,i)*d*(1.0-p.x),1.0);\n" +
                    "}";


    public static final String swirl =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "#extension GL_OES_standard_derivatives : enable\n" +
                    "//converted by batblaster\n" +
                    "\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "uniform vec2 resolution;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform sampler2D s_texture;\n" +
                    "\n" +
                    "float factor = 1.0;\n" +
                    "vec3 color = vec3(0.2, 0.5, 1.0);\n" +
                    "\n" +
                    "vec4 t(vec2 uv)\n" +
                    "{\n" +
                    "    float j = sin(uv.y * 3.14 + time * 5.0);\n" +
                    "    float i = sin(uv.x * 15.0 - uv.y * 2.0 * 3.14 + time * 3.0);\n" +
                    "    float n = -clamp(i, -0.2, 0.0) - 0.0 * clamp(j, -0.2, 0.0);\n" +
                    "    \n" +
                    "    return 3.5 * (vec4(color, 1.0) * n);\n" +
                    "}\n" +
                    "\n" +
                    "void main( void )\n" +
                    "{\n" +
                    "    float aspectRatio = resolution.x / resolution.y;\n" +
                    "    vec2 p = -1.0 + 2.0 * gl_FragCoord.xy / resolution.xy;\n" +
                    "    p.x *= aspectRatio;\n" +
                    "    vec2 uv;\n" +
                    "    \n" +
                    "    float r = sqrt(dot(p, p));\n" +
                    "    float a = atan(\n" +
                    "        p.y * (0.3 + 0.1 * cos(time * 2.0 + p.y)),\n" +
                    "        p.x * (0.3 + 0.1 * sin(time + p.x))\n" +
                    "    ) + time;\n" +
                    "    \n" +
                    "    uv.x = time + 1.0 / (r + .01);\n" +
                    "    uv.y = 4.0 * a / 3.1416;\n" +
                    "    \n" +
                    "    gl_FragColor = mix(vec4(0.0), t(uv) * r * r * 2.0, factor);\n" +
                    "}";
    public static final String balls =
            "precision highp float;\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "float ball(vec2 p, float fx, float fy, float ax, float ay) {\n" +
                    "    vec2 r = vec2(p.x + cos(time * fx) * ax, p.y + sin(time * fy) * ay);\t\n" +
                    "    return 0.09 / length(r);\n" +
                    "}\n" +
                    "\n" +
                    "void main(void) {\n" +
                    "    vec2 q = gl_FragCoord.xy / resolution.xy;\n" +
                    "    vec2 p = -1.0 + 2.0 * q;\t\n" +
                    "    p.x\t*= resolution.x / resolution.y;\n" +
                    "\n" +
                    "    float col = 0.0;\n" +
                    "    col += ball(p, 1.0, 2.0, 0.1, 0.2);\n" +
                    "    col += ball(p, 1.5, 2.5, 0.2, 0.3);\n" +
                    "    col += ball(p, 2.0, 3.0, 0.3, 0.4);\n" +
                    "    col += ball(p, 2.5, 3.5, 0.4, 0.5);\n" +
                    "    col += ball(p, 3.0, 4.0, 0.5, 0.6);\t\n" +
                    "    col += ball(p, 1.5, 0.5, 0.6, 0.7);\n" +
                    "    col += ball(p, 0.5, 3.1, 1.6, 0.9);\n" +
                    "    col += ball(p, 0.5, 2.1, 1.0, 0.0);\n" +
                    "    col += ball(p, 0.3, 1.9, 0.9, 0.99);\n" +
                    "\t\n" +
                    "    col *= 0.3;\t\n" +
                    "\t\n" +
                    "    gl_FragColor = vec4(col*0.6, col , col, 1.0);}";


    public static final String river =
            "precision mediump float;\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "float sinx(float x) {\n" +
                    "\tfloat v=sin(x);\n" +
                    "\treturn sign(v)*pow(abs(v),0.6);\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\tvec2 position = (2.0 * gl_FragCoord.xy - resolution.xy) / min(resolution.x, resolution.y);\n" +
                    "\n" +
                    "\tvec3 origin = vec3(4.0 * sin(time / 3.0), 0.0, -time * 2.0);\n" +
                    "\tvec3 dir = normalize(vec3(position.x, -position.y, -1.0));\n" +
                    "\tdir.yz = vec2(dir.y - dir.z, dir.z + dir.y) / sqrt(2.0);\n" +
                    "\n" +
                    "\tvec3 allcol = vec3(0.0);\n" +
                    "\tfor(int i = 1; i <= 3; i++) {\n" +
                    "\t\tfloat dist = (3.0 + float(2 * i)) / dir.y;\n" +
                    "\t\tif(dist > 0.0) {\n" +
                    "\t\t\tvec3 pos = dir * dist + origin;\n" +
                    "\t\t\tfloat c = pow(abs(sin(pos.x * 0.14 + float(i * i) + 0.6*sinx(pos.z / 3.0 * float(i + 3)))), 4.0) * 20.0 / dist / dist;\n" +
                    "\t\t\tvec3 col = vec3(1.0, 1.5, 2.0) * c;\n" +
                    "\t\t\tallcol += col;\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "\tgl_FragColor = vec4(allcol, 0.4);\n" +
                    "}";

    public static final String tunnel2 =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "#extension GL_OES_standard_derivatives : enable\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "float iGlobalTime = time;\n" +
                    "vec2 iResolution = resolution;\n" +
                    "\n" +
                    "// built for the Ello gif contest:\n" +
                    "// https://ello.co/medialivexello/post/gif-exhibition\n" +
                    "// Converted by Batblaster\n" +
                    "\n" +
                    "#define PI 3.141592653589793\n" +
                    "#define TAU 6.283185307179586\n" +
                    "\n" +
                    "// from iq / bookofshaders\n" +
                    "float cubicPulse( float c, float w, float x ){\n" +
                    "    x = abs(x - c);\n" +
                    "    if( x>w ) return 0.0;\n" +
                    "    x /= w;\n" +
                    "    return 1.0 - x*x*(3.0-2.0*x);\n" +
                    "}\n" +
                    "\n" +
                    "void mainImage( out vec4 fragColor, in vec2 fragCoord )\n" +
                    "{\n" +
                    "    float time = iGlobalTime * 0.55;\n" +
                    "    float rainbowSpeed = 5.0;\n" +
                    "    float colIntensity = 0.15;\n" +
                    "    \n" +
                    "    //////////////////////////////////////////////////////\n" +
                    "    // Create tunnel coordinates (p) and remap to normal coordinates (uv)\n" +
                    "    // Technique from @iq: https://www.shadertoy.com/view/Ms2SWW\n" +
                    "\t// and a derivative:   https://www.shadertoy.com/view/Xd2SWD\n" +
                    "    vec2 p = (-iResolution.xy + 2.0*fragCoord)/iResolution.y;\t\t// normalized coordinates (-1 to 1 vertically)\n" +
                    "    vec2 uvOrig = p;\n" +
                    "    // added twist by me ------------\n" +
                    "    float rotZ = 1. - 0.23 * sin(1. * cos(length(p * 1.5)));\n" +
                    "    p *= mat2(cos(rotZ), sin(rotZ), -sin(rotZ), cos(rotZ));\n" +
                    "\t//-------------------------------\n" +
                    "    float a = atan(p.y,p.x);\t\t\t\t\t\t\t\t\t\t\t\t// angle of each pixel to the center of the screen\n" +
                    "    float rSquare = pow( pow(p.x*p.x,4.0) + pow(p.y*p.y,4.0), 1.0/8.0 );\t// modified distance metric (http://en.wikipedia.org/wiki/Minkowski_distance)\n" +
                    "    float rRound = length(p);\n" +
                    "    float r = mix(rSquare, rRound, 0.5 + 0.5 * sin(time * 2.)); \t\t\t// interp between round & rect tunnels\n" +
                    "    vec2 uv = vec2( 0.3/r + time, a/3.1415927 );\t\t\t\t\t\t\t// index texture by (animated inverse) radious and angle\n" +
                    "    //////////////////////////////////////////////////////\n" +
                    "\n" +
                    "    // subdivide to grid\n" +
                    "    uv += vec2(0., 0.25 * sin(time + uv.x * 1.2));\t\t\t// pre-warp\n" +
                    "    uv /= vec2(1. + 0.0002 * length(uvOrig));\n" +
                    "    vec2 uvDraw = fract(uv * 12.);\t\t\t\t\t\t\t// create grid\n" +
                    "\n" +
                    "    // draw lines\n" +
                    "\tfloat col = cubicPulse(0.5, 0.06, uvDraw.x);\n" +
                    "    col = max(col, cubicPulse(0.5, 0.06, uvDraw.y));\n" +
                    "        \n" +
                    "    // darker towards center, light towards outer\n" +
                    "    col = col * r * 0.8;\n" +
                    "    col += colIntensity * length(uvOrig);\n" +
                    "    // NEW!\n" +
                    "    // sine function creates monotonous sweep across color band, phase differences of 0, 120 and 240 degrees are added\n" +
                    "    fragColor = vec4(vec3(col * sin(time*rainbowSpeed), col * sin(time*rainbowSpeed + 2.0*(PI/3.0)), col * sin(time*rainbowSpeed + 4.0*(PI/3.0))), 1.);\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\n" +
                    "\tmainImage(gl_FragColor,gl_FragCoord.xy);\n" +
                    "}";
    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left      0
            -0.5f, -0.5f, 0.0f,  // bottom left   1
            0.5f, -0.5f, 0.0f,  // bottom right  2
            0.5f,  0.5f, 0.0f}; // top right     3

    /*
            -0.5f, 0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            0.5f, 0.5f, 0.0f}; // top right*/

    private static short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    public BackGround(TYPE type) {
        //super(squareCoords,drawOrder,  vs_Image , fs_Image, resourceId);
        super("gb", type, GameView.metrics.widthPixels/2f, GameView.metrics.heightPixels/2f, 0, 0);
        if(programs.size() == 0)
        {
            programs.add(Shape.createProgram(vs_Image, fs_Image, -1));
            programs.add(Shape.createProgram(vs_Image, fs_Image_effect, -1));
            programs.add(Shape.createProgram(vs_Image, effect, -1));
            programs.add(Shape.createProgram(vs_Image, effect3, -1));
            programs.add(Shape.createProgram(vs_Image, effect2, -1));
            programs.add(Shape.createProgram(vs_Image, swirl, -1));
            programs.add(Shape.createProgram(vs_Image, balls, -1));
            programs.add(Shape.createProgram(vs_Image, river, -1));
            programs.add(Shape.createProgram(vs_Image, tunnel2, -1));
        }
        if(resourceId == R.drawable.greengem ||
                resourceId == R.drawable.redgem ||
                resourceId == R.drawable.orangegem ||
                resourceId == R.drawable.bluegem){
            width /= 10;
            height /= 3;
        }
    }
}
