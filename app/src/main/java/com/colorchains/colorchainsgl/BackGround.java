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
    public static final String shootingStars =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "const float Tau\t\t= 6.2832;\n" +
                    "const float speed\t= .02;\n" +
                    "const float density\t= .04;\n" +
                    "const float shape\t= .06;\n" +
                    "\n" +
                    "float random( vec2 seed ) {\n" +
                    "    return fract(sin(dot(seed.xy ,vec2(12.9898,78.233))) * 43758.5453);\n" +
                    "}\n" +
                    "\n" +
                    "float Cell(vec2 coord) {\n" +
                    "\tvec2 cell = fract(coord) * vec2(.5,2.) - vec2(.0,.5);\n" +
                    "\treturn (1.-length(cell*2.-1.))*step(random(floor(coord)),density)*2.;\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) {\n" +
                    "\n" +
                    "\tvec2 p = gl_FragCoord.xy / resolution  - 0.5;\n" +
                    "\t\n" +
                    "\tfloat a = fract(atan(p.x, p.y) / Tau);\n" +
                    "\tfloat d = pow(length(p),0.5);\n" +
                    "\t\n" +
                    "\tvec2 coord = vec2(pow(d, shape), a)*256.;\n" +
                    "\tvec2 delta = vec2(-time*speed*256., .5);\n" +
                    "\t\n" +
                    "\tfloat c = 0.;\n" +
                    "\tfor(int i=0; i<9; i++) {\n" +
                    "\t\tcoord += delta;\n" +
                    "\t\tc = max(c, Cell(coord));\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tgl_FragColor = vec4(c*d);\n" +
                    "\t\n" +
                    "}";
    public static final String rain =
            "//--- hatsuyuki ---\n" +
                    "// by Catzpaw 2016\n" +
                    "precision mediump float;\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "float snow(vec2 uv,float scale)\n" +
                    "{\n" +
                    "\tfloat w=smoothstep(1.,0.,-uv.y*(scale/10.));if(w<.1)return 0.;\n" +
                    "\tuv+=time/scale;uv.y+=time*2./scale;uv.x+=sin(uv.y+time*.5)/scale;\n" +
                    "\tuv*=scale;vec2 s=floor(uv),f=fract(uv),p;float k=3.,d;\n" +
                    "\tp=.5+.35*sin(11.*fract(sin((s+p+scale)*mat2(7,3,6,5))*5.))-f;d=length(p);k=min(d,k);\n" +
                    "\tk=smoothstep(0.,k,sin(f.x+f.y)*0.01);\n" +
                    "    \treturn k*w;\n" +
                    "}\n" +
                    "\n" +
                    "void main(void){\n" +
                    "\tvec2 uv=(gl_FragCoord.xy*2.-resolution.xy)/min(resolution.x,resolution.y); \n" +
                    "\tvec3 finalColor=vec3(0);\n" +
                    "\tfloat c=smoothstep(1.,0.3,clamp(uv.y*.3+.8,0.,.75));\n" +
                    "\tc+=snow(uv,30.)*.0;\n" +
                    "\tc+=snow(uv,20.)*.0;\n" +
                    "\tc+=snow(uv,15.)*.0;\n" +
                    "\tc+=snow(uv,10.);\n" +
                    "\tc+=snow(uv,8.);\n" +
                    "\tc+=snow(uv,6.);\n" +
                    "\tc+=snow(uv,5.);\n" +
                    "\tfinalColor=(vec3(c));\n" +
                    "\tfinalColor *= vec3(.7, 0.5, 1.);\n" +
                    "\tgl_FragColor = vec4(finalColor,1);\n" +
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

    public static final String swirl2 =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "//nuclear throne tunnel\n" +
                    "//2017.01.29 tigrou dot ind at gmail dot com\n" +
                    "#extension GL_OES_standard_derivatives : enable\n" +
                    "\n" +
                    "uniform float time;\n" +
                    "uniform vec2 mouse;\n" +
                    "uniform vec2 resolution;\n" +
                    "\n" +
                    "vec4 pattern(vec2 pos, float ang) \n" +
                    "{\n" +
                    "        pos = vec2(pos.x * cos(ang) - pos.y * sin(ang), pos.y * cos(ang) + pos.x * sin(ang));\t\n" +
                    "\t\n" +
                    "\t//if(length(pos) < 0.2)\n" +
                    "\tif(abs(pos.x) < 0.2 && abs(pos.y) < 0.2)\n" +
                    "\t   return vec4(0.0, 0.0, 0.0, 0.0);\n" +
                    "\telse if((abs(pos.x) - abs(pos.y)) > 0.0)\n" +
                    "\t   return vec4(0.59, 0.45, 0.05, 1.0);\n" +
                    "\telse\n" +
                    "\t   return vec4(0.27, 0.07, 0.39, 1.0);\t\t\t\n" +
                    "}\n" +
                    "\n" +
                    "void main( void ) \n" +
                    "{\n" +
                    "\tvec2 pos = ( gl_FragCoord.xy / resolution.xy ) - vec2(0.5, 0.5);\n" +
                    "\tvec4 color = vec4(0.0);\n" +
                    "\t\n" +
                    "\tfor(float i = 0.01 ; i < 1.0 ; i += 0.05)\n" +
                    "\t{\n" +
                    "\t\tfloat o = 1.0 - i;\n" +
                    "\t\tvec2 offset = vec2(o*cos(o*2.0+time)*0.5, o*sin(o*2.0+time)*0.5);\n" +
                    "\t\tvec4 res = pattern(pos/vec2(i*i*2.7)+offset, i*10.0+time);\n" +
                    "\t\tif(res.a > 0.0)\n" +
                    "\t\t     color = res*i*2.7;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tgl_FragColor = color;\n" +
                    "}";
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
    public static final String greenWater =
            "precision mediump float;\n" +
                    "\n" +
                    "        uniform float     time;\n" +
                    "        uniform vec2      resolution;\n" +
                    "        uniform vec2      mouse;\n" +
                    "\tvarying vec2 surfacePosition;\n" +
                    "\n" +
                    "        #define MAX_ITER 5\n" +
                    "\n" +
                    "        void main( void )\n" +
                    "        {\n" +
                    "            vec2 v_texCoord = gl_FragCoord.xy / resolution;\n" +
                    "\n" +
                    "            vec2 p =  v_texCoord * 8.0 - vec2(20.0);\n" +
                    "\t\tp = (surfacePosition - vec2(1.5))* 8.0;\n" +
                    "            vec2 i = p;\n" +
                    "            float c = 1.0;\n" +
                    "            float inten = .03;\n" +
                    "\n" +
                    "            for (int n = 0; n < MAX_ITER; n++)\n" +
                    "            {\n" +
                    "                float t = time * (1.0 - (3.0 / float(n+1)));\n" +
                    "\n" +
                    "                i = p + vec2(cos(t - i.x) + sin(t + i.y),\n" +
                    "                sin(t - i.y) + cos(t + i.x));\n" +
                    "\t\t    \n" +
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
                    "}       ";
    public static final String tunnel =
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
                    "#define HorizontalAmplitude\t\t1.00\n" +
                    "#define VerticleAmplitude\t\t0.80\n" +
                    "#define HorizontalSpeed\t\t\t0.90\n" +
                    "#define VerticleSpeed\t\t\t3.10\n" +
                    "#define ParticleMinSize\t\t\t1.76\n" +
                    "#define ParticleMaxSize\t\t\t1.71\n" +
                    "#define ParticleBreathingSpeed\t\t0.30\n" +
                    "#define ParticleColorChangeSpeed\t0.70\n" +
                    "#define ParticleCount\t\t\t7.0\n" +
                    "#define ParticleColor1\t\t\tvec3(9.0, 5.0, 3.0)\n" +
                    "#define ParticleColor2\t\t\tvec3(1.0, 3.0, 9.0)\n" +
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
                    "    float a = 2.0 * atan( p.x, p.y  );\n" +
                    "    float po = 2.0;\n" +
                    "    float px = pow( p.x*p.x, po );\n" +
                    "    float py = pow( p.y*p.y, po );\n" +
                    "    float r = pow( px + py, 1.0/(2.0*po) );    \n" +
                    "    vec2 uvp = vec2( 1.0/r + (time*scrollSpeed), a + (time*rotateSpeed));\t\n" +
                    "    vec3 finalColor = checkerBoard( uvp, p ).xyz;\n" +
                    "    finalColor *= r;\n" +
                    "\n" +
                    "    return finalColor;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 particles( vec2 uv )\n" +
                    "{\n" +
                    "\tvec2 pos = uv * 2.0 - 1.0;\n" +
                    "\tpos.x *= (resolution.x / resolution.y);\n" +
                    "\t\n" +
                    "\tvec3 c = vec3( 0, 0, 0 );\n" +
                    "\t\n" +
                    "\tfor( float i = 1.0; i < ParticleCount+1.0; ++i )\n" +
                    "\t{\n" +
                    "\t\tfloat cs = cos( time * HorizontalSpeed * (i/ParticleCount) ) * HorizontalAmplitude;\n" +
                    "\t\tfloat ss = sin( time * VerticleSpeed   * (i/ParticleCount) ) * VerticleAmplitude;\n" +
                    "\t\tvec2 origin = vec2( cs , ss );\n" +
                    "\t\t\n" +
                    "\t\tfloat t = sin( time * ParticleBreathingSpeed * i ) * 0.5 + 0.5;\n" +
                    "\t\tfloat particleSize = mix( ParticleMinSize, ParticleMaxSize, t );\n" +
                    "\t\tfloat d = clamp( sin( length( pos - origin )  + particleSize ), 0.0, particleSize);\n" +
                    "\t\t\n" +
                    "\t\tfloat t2 = sin( time * ParticleColorChangeSpeed * i ) * 0.5 + 0.5;\n" +
                    "\t\tvec3 color = mix( ParticleColor1, ParticleColor2, t2 );\n" +
                    "\t\tc += color * pow( d, 70.0 );\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\treturn c;\n" +
                    "}\n" +
                    "\n" +
                    "void main(void)\n" +
                    "{\n" +
                    "    vec2 uv = gl_FragCoord.xy / resolution.xy;\n" +
                    "    float timeSpeedX = time * 0.3;\n" +
                    "    float timeSpeedY = time * 0.2;\n" +
                    "    vec2 p = uv + vec2( -0.50+cos(timeSpeedX)*0.2, -0.5-sin(timeSpeedY)*0.3 );\n" +
                    "\n" +
                    "    vec3 finalColor = tunnel( p , 1.0, 0.0);\n" +
                    "\n" +
                    "\n" +
                    "    timeSpeedX = time * 0.30001;\n" +
                    "    timeSpeedY = time * 0.20001;\n" +
                    "    p = uv + vec2( -0.50+cos(timeSpeedX)*0.2, -0.5-sin(timeSpeedY)*0.3 );\n" +
                    "    \n" +
                    "\t\n" +
                    "\tfinalColor += particles( uv );\n" +
                    "\t\n" +
                    "    gl_FragColor = vec4( finalColor, 1.0 );\n" +
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

    public BackGround(Integer resourceId) {
        //super(squareCoords,drawOrder,  vs_Image , fs_Image, resourceId);
        super("gb", TYPE.IMAGE, GameView.metrics.widthPixels/2f, GameView.metrics.heightPixels/2f, 0, 0);
        if(programs.size() == 1)
        {
            //programs.add(Shape.createProgram(vs_Image, fs_Image, -1));
            programs.add(Shape.createProgram(vs_Image, fs_Image_effect, -1));
            programs.add(Shape.createProgram(vs_Image, effect, -1));
            programs.add(Shape.createProgram(vs_Image, effect3, -1));
            programs.add(Shape.createProgram(vs_Image, effect2, -1));
            programs.add(Shape.createProgram(vs_Image, shootingStars, -1));
            programs.add(Shape.createProgram(vs_Image, rain, -1));
            programs.add(Shape.createProgram(vs_Image, swirl, -1));
            programs.add(Shape.createProgram(vs_Image, balls, -1));
            programs.add(Shape.createProgram(vs_Image, swirl2, -1));
            programs.add(Shape.createProgram(vs_Image, river, -1));
            programs.add(Shape.createProgram(vs_Image, greenWater, -1));
            programs.add(Shape.createProgram(vs_Image, tunnel, -1));
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
