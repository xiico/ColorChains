package com.colorchains.colorchainsgl;

import android.renderscript.Element;

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
                    "  st.x += cos(time*2.)/2.;\n" +
                    "  st.y += sin(time*2.)/2.;\n" +
                    "  // See this page (https://goo.gl/KJ9ScK) about the formula below.\n" +
                    "  float f = abs(sin(time)) * 1.0 / length(st);\n" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord ) * vec4(vec3(f), 1.0);\n" +
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
//                    "    col += ball(p, 2.5, 3.5, 0.4, 0.5);\n" +
//                    "    col += ball(p, 3.0, 4.0, 0.5, 0.6);\t\n" +
//                    "    col += ball(p, 1.5, 0.5, 0.6, 0.7);\n" +
//                    "    col += ball(p, 0.5, 3.1, 1.6, 0.9);\n" +
//                    "    col += ball(p, 0.5, 2.1, 1.0, 0.0);\n" +
//                    "    col += ball(p, 0.3, 1.9, 0.9, 0.99);\n" +
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
    private static final String hive =
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
                    "\n" +
                    "\n" +
                    "float hexGrid(vec2 p)\n" +
                    "{\n" +
                    "  p.x *= 1.1547;\n" +
                    "  p.y += mod(floor(p.x), 2.)*0.5;\n" +
                    "  p = abs((mod(p, 1.0) - 0.5));\n" +
                    "  return abs(max(p.x*1.5 + p.y, p.y*2.0)-1.0);\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\tvec2 p = ( gl_FragCoord.xy * 2.0 - resolution.xy ) / min(resolution.x, resolution.y);\n" +
                    "\n" +
                    "\tfloat color = hexGrid(p * 3.0 + vec2(time*0.5, time*0.3));\n" +
                    "\t\n" +
                    "\tvec4 finColor = vec4(p.x*max(p.x, p.y)-p.y, p.y, 0.4 + 0.8*sin(time), 1.0);\n" +
                    "\t\t\n" +
                    "\tgl_FragColor = vec4(color * finColor);\n" +
                    "\n" +
                    "\t\n" +
                    "\n" +
                    "}";
    private static final String waves =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\tvec2 pos = gl_FragCoord.xy / resolution.xy;\n" +
                    "\tpos.y -= 0.6;\n" +
                    "\tvec3 c = vec3(0,0,0);\n" +
                    "\tc = mix(vec3(0,1,1), vec3(0,0.1,0.1), pos.y);\n" +
                    "\tfloat v = sin((pos.x + time*0.2) * 5.0)*0.2 + sin((pos.x * 3.0+ time*0.1) * 5.0)*0.2;\n" +
                    "\tif(pos.y < v){\n" +
                    "\t\tc = mix(c, vec3(0,0.5,0.5), 0.2);\n" +
                    "\t}\n" +
                    "\tv = sin((pos.x + time*0.1) * 5.0)*0.1 + sin((pos.x * 3.0+ time*0.05) * 5.0)*0.1;\n" +
                    "\tif(pos.y < v){\n" +
                    "\t\tc = mix(c, vec3(0,0.5,0.5), 0.2);\n" +
                    "\t}\n" +
                    "\tgl_FragColor = vec4(c, 13.0);\n" +
                    "\n" +
                    "}";
    public static final String greenWater =
            "precision mediump float;\n" +
                    "\n" +
                    "        uniform float     time;\n" +
                    "        uniform vec2      resolution;\n" +
                    "        uniform vec2      mouse;\n" +
                    "\n" +
                    "        #define MAX_ITER 1\n" +
                    "\n" +
                    "        void main( void )\n" +
                    "        {\n" +
                    "            vec2 v_texCoord = gl_FragCoord.xy / resolution;\n" +
                    "\n" +
                    "            vec2 p =  v_texCoord * 8.0 - vec2(20.0);\n" +
                    "            vec2 i = p;\n" +
                    "            float c = 1.0;\n" +
                    "            float inten = .05;\n" +
                    "\n" +
                    "            for (int n = 0; n < MAX_ITER; n++)\n" +
                    "            {\n" +
                    "                float t = time * (1.0 - (3.0 / float(n+1)));\n" +
                    "\n" +
                    "                i = p + vec2(cos(t - i.x) + sin(t + i.y),\n" +
                    "                sin(t - i.y) + cos(t + i.x));\n" +
                    "                c += 1.0/length(vec2(p.x / (sin(i.x+t)/inten),\n" +
                    "                p.y / (cos(i.y+t)/inten)));\n" +
                    "            }\n" +
                    "\n" +
                    "            c /= float(MAX_ITER);\n" +
                    "            c = 1.5 - sqrt(c);\n" +
                    "\n" +
                    "            vec4 texColor = vec4(0.02, 0.15, 0.02, 1.);\n" +
                    "\n" +
                    "            texColor.rgb *= (1.0 / (1.0 - (c + 0.05)));\n" +
                    "\n" +
                    "            gl_FragColor = texColor;\n" +
                    "        }";
    private static final String rainbow =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "uniform vec2 resolution;\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "\tvec2 p=(3.0*gl_FragCoord.xy-resolution)/max(resolution.x,resolution.y);\n" +
                    "\tfor(int i=1;i<10;i++)\n" +
                    "\t{\n" +
                    "\t\tvec2 newp=p;\n" +
                    "\t\tnewp.x+=0.6/float(i)*sin(float(i)*p.y+time/3.0+0.3*float(i))+1.0;\t\t\n" +
                    "\t\tnewp.y+=0.6/float(i)*sin(float(i)*p.x+time/40.0+0.3*float(i+10))-0.0/20.0+15.0;\n" +
                    "\t\tp=newp;\n" +
                    "\t}\n" +
                    "\tvec3 col=vec3(0.5*sin(4.0*p.x)+0.5,0.5*sin(2.0*p.y)+0.5,sin(2.1*(p.x+p.y)));\n" +
                    "\tgl_FragColor=vec4(col, 1.0);\n" +
                    "}";
    private static final String tunnel3 =
            "// Endless Tunnel\n" +
                    "// By: Brandon Fogerty\n" +
                    "// bfogerty at gmail dot com\n" +
                    "\n" +
                    "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "\n" +
                    "vec3 checkerBoard( vec2 uv, vec2 pp )\n" +
                    "{\n" +
                    "    vec2 p = floor( uv * 4.6 );\n" +
                    "    float t = mod( p.x + p.y, 2.2);\n" +
                    "    vec3 c = vec3(t+pp.x, t+pp.y, t+(pp.x*pp.y));\n" +
                    "\n" +
                    "    return c;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 tunnel( vec2 p, float scrollSpeed, float rotateSpeed )\n" +
                    "{    \n" +
                    "    float a  = 0.0 * atan( p.x, p.y  );\n" +
                    "    float po = 1.0;\n" +
                    "    float px = pow( p.x*p.x, po );\n" +
                    "    float py = pow( p.y*p.y, po );\n" +
                    "    float r  = pow( px + py, 1.0/(4.0*po) );    \n" +
                    "    vec2 uvp = vec2( 1.0/r + (time*scrollSpeed), a + (time*rotateSpeed));\t\n" +
                    "    vec3 finalColor = checkerBoard( uvp, p ).xyz;\n" +
                    "    finalColor *= r;\n" +
                    "\n" +
                    "    return finalColor;\n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    "void main(void)\n" +
                    "{\n" +
                    "    vec2 uv = gl_FragCoord.xy / resolution.xy;\n" +
                    "    vec2 p = uv + vec2( -0.5, -0.5 );\n" +
                    "\n" +
                    "    vec3 finalColor = tunnel( p , 0.1, 0.0);\n" +
                    "\t\n" +
                    "    gl_FragColor = vec4( finalColor, 1.0 );\n" +
                    "}";
    private static final String rainbow2 =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "#extension GL_OES_standard_derivatives : enable\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "#define ROTATE_SPEED 0.2\n" +
                    "#define MIX_SPEED 1.5\n" +
                    "#define ZOOM 1.2\n" +
                    "\n" +
                    "#define SMOOTHERSTEP(x) ((x) * (x) * (x) * ((x) * ((x) * 6.0 - 15.0) + 10.0))\n" +
                    "\n" +
                    "vec3 bump3y(in vec3 x, in vec3 yoffset)\n" +
                    "{\n" +
                    "\tvec3 y = vec3(1.0) - x * x;\n" +
                    "\ty = clamp(y - yoffset, 0.0, 1.9);\n" +
                    "\treturn y;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 spectral_zucconi(float x)\n" +
                    "{\n" +
                    "\tconst vec3 cs = vec3(3.54541723, 2.86670055, 2.29421995);\n" +
                    "\tconst vec3 xs = vec3(0.69548916, 0.49416934, 0.28269708);\n" +
                    "\tconst vec3 ys = vec3(0.02320775, 0.15936245, 0.53520021);\n" +
                    "\n" +
                    "\treturn bump3y(cs * (x - xs), ys);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 spectral_zucconi6(in float x)\n" +
                    "{\n" +
                    "\tconst vec3 c1 = vec3(3.54585104, 2.93225262, 2.41593945);\n" +
                    "\tconst vec3 x1 = vec3(0.69549072, 0.49228336, 0.27699880);\n" +
                    "\tconst vec3 y1 = vec3(0.02312639, 0.15225084, 0.52607955);\n" +
                    "\n" +
                    "\tconst vec3 c2 = vec3(3.90307140, 3.21182957, 3.96587128);\n" +
                    "\tconst vec3 x2 = vec3(0.11748627, 0.86755042, 0.66077860);\n" +
                    "\tconst vec3 y2 = vec3(0.84897130, 0.88445281, 0.73949448);\n" +
                    "\n" +
                    "\treturn\n" +
                    "        bump3y(c1 * (x - x1), y1) +\n" +
                    "        bump3y(c2 * (x - x2), y2);\n" +
                    "}\n" +
                    "\n" +
                    "vec2 rotate(in vec2 point, in float rads)\n" +
                    "{\n" +
                    "\tfloat cs = cos(rads);\n" +
                    "\tfloat sn = sin(rads);\n" +
                    "\treturn point * mat2(cs, -sn, sn, cs);\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\tvec2 position = gl_FragCoord.xy / resolution.xy;\n" +
                    "\tvec2 cpos = (position * 2.0) - 1.0;\n" +
                    "\tvec2 rpos = rotate(cpos, time * ROTATE_SPEED);\n" +
                    "\trpos *= ZOOM;\n" +
                    "\tvec2 rposition = (rpos + 1.0) / 2.0;\n" +
                    "\n" +
                    "\tfloat w = (rposition.x + rposition.y) / 2.0;\n" +
                    "\n" +
                    "\tfloat period = mod(time * MIX_SPEED, 2.0);\n" +
                    "\n" +
                    "\tfloat reflectperiod = (period > 1.0) ? (2.0 - period) : period;\n" +
                    "\n" +
                    "\tvec3 s1 = spectral_zucconi(w);\n" +
                    "\tvec3 s2 = spectral_zucconi6(w);\n" +
                    "\n" +
                    "\tvec3 color = mix(s1, s2, SMOOTHERSTEP(reflectperiod));\n" +
                    "\n" +
                    "\tfloat alpha = 1.0;\n" +
                    "\tgl_FragColor = vec4(color, alpha);\n" +
                    "}";

    private static final String colorBalls = "#ifdef GL_ES\n" +
            "precision highp float;\n" +
            "#endif\n" +
            "\n" +
            "#extension GL_OES_standard_derivatives : enable\n" +
            "\n" +
            "uniform float time;\n" +
            "uniform vec2 mouse;\n" +
            "uniform vec2 resolution;\n" +
            "\n" +
            "vec4 draw_ball(vec2 ball_center, vec3 ball_color, float ball_rad, float freq, float fx, float fy, float rx, float ry){\n" +
            "\tvec2 position = ( gl_FragCoord.xy / resolution.xy );\n" +
            "\tfloat divider = resolution.x / resolution.y;\n" +
            "\tvec2 center = ball_center;\n" +
            "\tcenter.x += rx * cos(time * fx);\n" +
            "\tcenter.y += ry * sin(time * fy);\n" +
            "\tvec2 curpos = position - center;\n" +
            "\tcurpos.y /= divider;\n" +
            "\t\n" +
            "\tfloat length_ = dot(curpos, curpos);\n" +
            "\tfloat coly = 0.0;\n" +
            "\tfloat r2 = ball_rad * ball_rad;\n" +
            "\tcoly = r2 / length_;\n" +
            "\treturn vec4(ball_color * coly * (sin(time * freq) + 2.0), 1.0);\n" +
            "}\n" +
            "\n" +
            "void main( void ) {\n" +
            "\t// 2d coordinates.\n" +
            "\tgl_FragColor += draw_ball(vec2(0.5, 0.5), vec3(0.5, 0.05, 0.05), 0.05, 3.0, 3.0, 2.5, 0.1, 0.25);\n" +
            "\tgl_FragColor += draw_ball(vec2(0.7, 0.5), vec3(0.05, 0.5, 0.05), 0.04, 4.0, 1.0, 4.5, 0.2, 0.1);\n" +
            "\tgl_FragColor += draw_ball(vec2(0.3, 0.5), vec3(0.05, 0.05, 0.7), 0.04, 2.5, 5.0, 2.0, 0.1, 0.3);\n" +
//            "\tgl_FragColor += draw_ball(vec2(0.6, 0.5), vec3(0.05, 0.4, 0.05), 0.05, 3.5, 3.0, 2.0, 0.2, 0.3);\n" +
//            "\tgl_FragColor += draw_ball(vec2(0.25, 0.25), vec3(0.3, 0.23, 0.805), 0.04, 21.5, 1.0, 4.5, 0.2, 0.4);\n" +
//            "\tgl_FragColor += draw_ball(vec2(0.75, 0.45), vec3(0.3, 0.3, 0.085), 0.04, 2.5, 1.0, 4.5, 0.2, 0.4);\n" +
//            "\tgl_FragColor += draw_ball(vec2(0.45, 0.55), vec3(0.73, 0.53, 0.105), 0.04, 2.5, 1.0, 4.5, 0.2, 0.4);\n" +
//            "\tgl_FragColor += draw_ball(vec2(0.25, 0.65), vec3(0.53, 0.3, 0.205), 0.04, 2.5, 1.0, 4.5, 0.2, 0.4);\n" +
//            "\tgl_FragColor += draw_ball(vec2(0.55, 0.15), vec3(0.13, 0.73, 0.005), 0.04, 2.5, 1.0, 4.5, 0.2, 0.4);\n" +
            "}";

    private static final String wave = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "uniform float time;\n" +
            "uniform vec2 resolution;\n" +
            "\n" +
            "void main( void ) {\n" +
            "\n" +
            "\tvec2 uv = ( gl_FragCoord.xy / resolution.xy ) * 2.0 - 1.0;\n" +
            "\n" +
            "\tvec3 finalColor = vec3 ( 0.3, 0.5, 0.5 );\n" +
            "\t\n" +
            "\tfinalColor *= abs( 1.0 / (sin( uv.y + sin(uv.x+time) * .6) * 20.0) );\n" +
            "\t\n" +
            "\n" +
            "\tgl_FragColor = vec4( finalColor, 1.0 );\n" +
            "\n" +
            "}";

    private static final String waves2 = "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "#extension GL_OES_standard_derivatives : enable\n" +
            "\n" +
            "uniform float time;\n" +
            "uniform vec2 mouse;\n" +
            "uniform vec2 resolution;\n" +
            "\n" +
            "void main( void ) {\n" +
            "\n" +
            "\tvec2 pos = ( gl_FragCoord.xy / resolution.xy );\n" +
            "\n" +
            "\tfloat color_r = 0.0;\t\t\n" +
            "\tfloat color_g = 0.0;\t\t\n" +
            "\tfloat color_b = 0.0;\t\t\n" +
            "\t\n" +
            "\tfloat dist = (pos[1] - 0.4*sin((pos[0]+time/15.)*2.0) - 0.5);\n" +
            "\tdist = abs(dist);\n" +
            "\tcolor_r = pow(1.0 - dist, 5.0);\n" +
            "\tfloat dist1 = (pos[1] - 0.5*sin((pos[0]+time/10.0)*2.0) - 0.5);\n" +
            "\tdist1 = abs(dist1);\n" +
            "\tcolor_g = pow(1.0 - dist1, 5.0);\n" +
            "\tfloat dist2 = (pos[1] - 0.4*sin((pos[0]+time/5.)*2.0) - 0.5);\n" +
            "\tdist2 = abs(dist2);\n" +
            "\tcolor_b = pow(1.0 - dist2, 5.0);\n" +
            "\t\n" +
            "\tgl_FragColor = vec4( vec3( color_r, color_g, color_b ), 1.0 );\n" +
            "}";

    private static final String swirl2 = "#ifdef GL_ES\n" +
            "precision highp float;\n" +
            "#endif\n" +
            "\n" +
            "// Posted by Trisomie21\n" +
            "// modified by @hintz\n" +
            "\n" +
            "uniform float time;\n" +
            "uniform vec2 mouse;\n" +
            "uniform vec2 resolution;\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "\tfloat scale = resolution.y / 50.0;\n" +
            "\tfloat ring = 10.0;\n" +
            "\tfloat radius = resolution.x*1.0;\n" +
            "\tfloat gap = scale*.1;\n" +
            "\tvec2 pos = gl_FragCoord.xy - resolution.xy*.5;\n" +
            "\t\n" +
            "\tfloat d = length(pos);\n" +
            "\t\n" +
            "\t// Create the wiggle\n" +
            "\td += .2*(sin(pos.y*0.25/scale+time/2.)*sin(pos.x*0.25/scale+time*.5))*scale*5.0;\n" +
            "\t\n" +
            "\t// Compute the distance to the closest ring\n" +
            "\tfloat v = mod(d + radius/(ring*2.0), radius/ring);\n" +
            "\tv = abs(v - radius/(ring*2.0));\n" +
            "\t\n" +
            "\tv = clamp(v-gap, 0.0, 1.0);\n" +
            "\t\n" +
            "\td /= radius;\n" +
            "\tvec3 m = fract((d-1.0)*vec3(ring*-.5, -ring, ring*.25)*0.5);\n" +
            "\t\n" +
            "\tgl_FragColor = vec4(m*v, 1.0);\n" +
            "}";

    private static final String waves3 = "// Guyver\n" +
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "\n" +
            "uniform float time;\n" +
            "uniform float lowFreq;\n" +
            "uniform vec2 resolution;\n" +
            "\n" +
            "\n" +
            "vec3 SUN_1 = vec3(0.0,0.5,1.0);\n" +
            "vec3 SUN_2 = vec3(1.0,0.0,0.0);\n" +
            "vec3 SUN_3 = vec3(0.1,1.0,0.753);\n" +
            "vec3 SUN_4 = vec3(0.6,0.8,0.0);\n" +
            "\n" +
            "\n" +
            "float sigmoid(float x)\n" +
            "{\n" +
            "\treturn 1.5/(1. + exp2(-x)) - 1.;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "void main( void ) \n" +
            "{\n" +
            "\tvec2 position = gl_FragCoord.xy;\n" +
            "\tvec2 aspect = vec2(resolution/resolution );\n" +
            "\tposition -= 0.5*resolution;\n" +
            "\tvec2 position2 = 0.5 + (position-0.5)/resolution*3.;\n" +
            "\tposition *= .05;\n" +
            "\tposition2 *= .05;\n" +
            "\tfloat filter = sigmoid(pow(2.,7.5)*(length((position/resolution + 0.5)*aspect) - 0.015))*0.5 +0.5 +lowFreq*lowFreq;\n" +
            "\tposition = mix(position, position2, filter) - 0.5;\n" +
            "\n" +
            "\tvec3 color = vec3(0.);\n" +
            "\tfloat angle = atan(position.y,position.x);\n" +
            "\tfloat d = length(position);\n" +
            "\tfloat t = time * .5;\n" +
            "\tcolor += 0.08/length(vec2(.05,0.5*position.y+sin(position.x*10.+t*-6.)))*SUN_3; \n" +
            "\tcolor += 0.07/length(vec2(.06,1.0*position.y+sin(position.x*10.+t*-2.)))*SUN_1; // I'm sure there's an easier way to do this, this just happened to look nice and blurry.\n" +
            "\tcolor += 0.06/length(vec2(.07,2.0*position.y+sin(position.x*10.+t*2.)))*SUN_2;\n" +
            "\tcolor += 0.05/length(vec2(.08,4.0*position.y+sin(position.x*10.+t*6.)))*SUN_3;\n" +
            "\tcolor += 0.04/length(vec2(.09,8.0*position.y+sin(position.x*10.+t*10.)))*SUN_4;" +
            "\t\n" +
            "\tgl_FragColor = vec4(color, 1.0);\n" +
            "}";

    public BackGround() {
        //super(squareCoords,drawOrder,  vs_Image , fs_Image, resourceId);
        super("gb", TYPE.BACKGROUND, GameView.metrics.widthPixels/2f, GameView.metrics.heightPixels/2f, 0, 0);
        if(programs.size() == 0)
        {
            programs.add(Shape.createProgram(vs_Image, fs_Image, -1));
            //programs.add(Shape.createProgram(vs_Image, fs_Image_effect, -1));
            //programs.add(Shape.createProgram(vs_Image, effect, -1));
            programs.add(Shape.createProgram(vs_Image, effect3, -1));
            //programs.add(Shape.createProgram(vs_Image, effect2, -1));
            programs.add(Shape.createProgram(vs_Image, swirl, -1));
            programs.add(Shape.createProgram(vs_Image, balls, -1));
            programs.add(Shape.createProgram(vs_Image, river, -1));
            programs.add(Shape.createProgram(vs_Image, tunnel2, -1));
            programs.add(Shape.createProgram(vs_Image, hive, -1));
            programs.add(Shape.createProgram(vs_Image, waves, -1));
            programs.add(Shape.createProgram(vs_Image, greenWater, -1));
            //programs.add(Shape.createProgram(vs_Image, rainbow, -1));
            programs.add(Shape.createProgram(vs_Image, tunnel3, -1));
            programs.add(Shape.createProgram(vs_Image, rainbow2, -1));
            programs.get(programs.size() - 1).setTimeLimit((float) (Math.PI*2f));
            programs.add(Shape.createProgram(vs_Image, colorBalls, -1));
            programs.get(programs.size() - 1).setTimeLimit((float) (Math.PI*2f));
            programs.get(programs.size() - 1).setTimeStep(.005f);
            programs.add(Shape.createProgram(vs_Image, wave, -1));
            programs.add(Shape.createProgram(vs_Image, waves2, -1));
            programs.add(Shape.createProgram(vs_Image, swirl2, -1));
            programs.add(Shape.createProgram(vs_Image, waves3, -1));
        }
        if(resourceId == R.drawable.greengem ||
                resourceId == R.drawable.redgem ||
                resourceId == R.drawable.orangegem ||
                resourceId == R.drawable.bluegem){
            setWidth(getWidth() / 10f) ;
            setWidth(getWidth() / 3);
        }
    }
}
