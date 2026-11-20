package com.manyl.mod.client.renderer;

import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Нативный Smooth-Mesh 3D-движок для режима !@ADMIN_OP (пропорции C+, единая анатомия,
 * гладкий аниме-рендер лица и облегающей одежды без угловатых воксельных блоков).
 */
public final class ValkyrieSmoothMeshEngine {
    public static final Identifier SMOOTH_ATLAS = Identifier.fromNamespaceAndPath(
            "manyl_mod", "textures/entity/golem/valkyrie_op_smooth.png");

    private static final float PI = (float) Math.PI;
    private static final float TAU = PI * 2.0F;
    private static final float BUST_C = 1.78F;
    private static final float HIP_C = 1.48F;

    private ValkyrieSmoothMeshEngine() {}

    // =========================================================================
    //  Compact Baked Quad Mesh
    // =========================================================================

    private static final class QuadMesh {
        final float[] pos; // x,y,z per vertex
        final float[] nrm; // nx,ny,nz per vertex
        final float[] uv;  // u,v per vertex
        final float[] ao;  // scalar AO + tint rgb packed (r,g,b per vertex)
        final int[] quads; // 4 vertex indices per quad

        QuadMesh(List<float[]> verts, List<int[]> quadList) {
            int vc = verts.size();
            this.pos = new float[vc * 3];
            this.nrm = new float[vc * 3];
            this.uv = new float[vc * 2];
            this.ao = new float[vc * 3];
            for (int i = 0; i < vc; i++) {
                float[] v = verts.get(i);
                this.pos[i * 3] = v[0];
                this.pos[i * 3 + 1] = v[1];
                this.pos[i * 3 + 2] = v[2];
                this.nrm[i * 3] = v[3];
                this.nrm[i * 3 + 1] = v[4];
                this.nrm[i * 3 + 2] = v[5];
                this.uv[i * 2] = v[6];
                this.uv[i * 2 + 1] = v[7];
                this.ao[i * 3] = v[8];
                this.ao[i * 3 + 1] = v[9];
                this.ao[i * 3 + 2] = v[10];
            }
            this.quads = new int[quadList.size() * 4];
            for (int i = 0; i < quadList.size(); i++) {
                int[] q = quadList.get(i);
                this.quads[i * 4] = q[0];
                this.quads[i * 4 + 1] = q[1];
                this.quads[i * 4 + 2] = q[2];
                this.quads[i * 4 + 3] = q[3];
            }
        }

        void emit(PoseStack.Pose pose, VertexConsumer vc, int light, float alpha, float tintR, float tintG, float tintB) {
            for (int i = 0; i < quads.length; i++) {
                int vi = quads[i];
                int p3 = vi * 3;
                int u2 = vi * 2;
                float r = Mth.clamp(ao[p3] * tintR, 0.0F, 1.0F);
                float g = Mth.clamp(ao[p3 + 1] * tintG, 0.0F, 1.0F);
                float b = Mth.clamp(ao[p3 + 2] * tintB, 0.0F, 1.0F);
                vc.addVertex(pose, pos[p3], pos[p3 + 1], pos[p3 + 2])
                        .setColor(r, g, b, alpha)
                        .setUv(uv[u2], uv[u2 + 1])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, nrm[p3], nrm[p3 + 1], nrm[p3 + 2]);
            }
        }
    }

    private static final class MeshBuilder {
        final List<float[]> verts = new ArrayList<>();
        final List<int[]> quads = new ArrayList<>();
        final float pivotX, pivotY, pivotZ;

        MeshBuilder(float px, float py, float pz) {
            this.pivotX = px;
            this.pivotY = py;
            this.pivotZ = pz;
        }

        int addVertex(float wx, float wy, float wz, float nx, float ny, float nz,
                      float u, float v, float r, float g, float b) {
            int idx = verts.size();
            verts.add(new float[]{wx - pivotX, wy - pivotY, wz - pivotZ, nx, ny, nz, u, v, r, g, b});
            return idx;
        }

        void addQuad(int a, int b, int c, int d) {
            quads.add(new int[]{a, b, c, d});
        }

        void computeSmoothNormals() {
            float[] nx = new float[verts.size()];
            float[] ny = new float[verts.size()];
            float[] nz = new float[verts.size()];
            for (int[] q : quads) {
                float[] p0 = verts.get(q[0]);
                float[] p1 = verts.get(q[1]);
                float[] p2 = verts.get(q[2]);
                float ax = p1[0] - p0[0], ay = p1[1] - p0[1], az = p1[2] - p0[2];
                float bx = p2[0] - p0[0], by = p2[1] - p0[1], bz = p2[2] - p0[2];
                float cx = ay * bz - az * by;
                float cy = az * bx - ax * bz;
                float cz = ax * by - ay * bx;
                for (int k = 0; k < 4; k++) {
                    int vi = q[k];
                    nx[vi] += cx;
                    ny[vi] += cy;
                    nz[vi] += cz;
                }
            }
            for (int i = 0; i < verts.size(); i++) {
                float len = (float) Math.sqrt(nx[i] * nx[i] + ny[i] * ny[i] + nz[i] * nz[i]);
                if (len > 1.0E-6F) {
                    float[] v = verts.get(i);
                    v[3] = nx[i] / len;
                    v[4] = ny[i] / len;
                    v[5] = nz[i] / len;
                }
            }
        }

        QuadMesh build() {
            computeSmoothNormals();
            return new QuadMesh(verts, quads);
        }
    }

    // =========================================================================
    //  Atlas UV Helper (4x4 Grid of 512x512 Tiles)
    // =========================================================================

    private static float tileU(int tx, float u) {
        return (tx + 0.008F + Mth.clamp(u, 0.0F, 1.0F) * 0.984F) * 0.25F;
    }

    private static float tileV(int ty, float v) {
        return (ty + 0.008F + Mth.clamp(v, 0.0F, 1.0F) * 0.984F) * 0.25F;
    }

    private static float smoothstep(float e0, float e1, float x) {
        float t = Mth.clamp((x - e0) / (e1 - e0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float lerpf(float a, float b, float t) {
        return a + (b - a) * Mth.clamp(t, 0.0F, 1.0F);
    }

    private static float gaussian(float x, float center, float sigma) {
        float d = (x - center) / sigma;
        return (float) Math.exp(-d * d);
    }

    // =========================================================================
    //  Cached Static Anatomy & Outfit Meshes
    // =========================================================================

    private static final float PELVIS_PY = 0.860F;
    private static final float WAIST_PY = 0.960F;
    private static final float HEAD_PY = 1.468F, HEAD_PZ = 0.005F;
    private static final float HIP_PX = 0.084F, HIP_PY = 0.845F, HIP_PZ = -0.005F;
    private static final float KNEE_PY = 0.455F, KNEE_PZ = 0.006F;
    private static final float SHLD_PX = 0.148F, SHLD_PY = 1.340F, SHLD_PZ = -0.002F;
    private static final float ELBOW_PY = 1.075F;
    private static final float WING_PX = 0.058F, WING_PY = 1.295F, WING_PZ = -0.075F;
    private static final float TAIL_PY = 0.865F, TAIL_PZ = -0.105F;

    private static final QuadMesh TORSO_BASE;
    private static final QuadMesh[] TAILORED_OUTFITS = new QuadMesh[7]; // 0=bikini,1=evening,2=maid,3=neko,4=bunny,5=succubus,6=negligee
    private static final QuadMesh BIKINI_TRIMS;
    private static final QuadMesh EVENING_SKIRT;
    private static final QuadMesh MAID_SKIRT;
    private static final QuadMesh NEGLIGEE_CHIFFON;

    private static final QuadMesh HEAD_MESH;
    private static final QuadMesh MAID_HEADBAND;
    private static final QuadMesh NEKO_EARS;
    private static final QuadMesh BUNNY_EARS;
    private static final QuadMesh SUCCUBUS_HORNS;

    private static final QuadMesh RIGHT_THIGH_SKIN, LEFT_THIGH_SKIN;
    private static final QuadMesh RIGHT_THIGH_STOCKING, LEFT_THIGH_STOCKING;
    private static final QuadMesh RIGHT_SHIN_SKIN, LEFT_SHIN_SKIN;
    private static final QuadMesh RIGHT_SHIN_STOCKING, LEFT_SHIN_STOCKING;

    private static final QuadMesh RIGHT_UPPER_ARM, LEFT_UPPER_ARM;
    private static final QuadMesh RIGHT_MAID_PUFF, LEFT_MAID_PUFF;
    private static final QuadMesh RIGHT_FOREARM, LEFT_FOREARM;
    private static final QuadMesh RIGHT_GLOVE_CUFF, LEFT_GLOVE_CUFF;

    private static final QuadMesh WING_RIGHT_FEATHER, WING_LEFT_FEATHER;
    private static final QuadMesh WING_RIGHT_BAT, WING_LEFT_BAT;
    private static final QuadMesh TAIL_NEKO, TAIL_BUNNY, TAIL_SUCCUBUS;

    static {
        TORSO_BASE = buildTorsoShell(0.0F, -1, false);
        for (int i = 0; i < 7; i++) {
            TAILORED_OUTFITS[i] = buildTorsoShell(0.0038F, i, true);
        }
        BIKINI_TRIMS = buildBikiniTrims();
        EVENING_SKIRT = buildEveningSkirt();
        MAID_SKIRT = buildMaidSkirt();
        NEGLIGEE_CHIFFON = buildNegligeeChiffon();

        HEAD_MESH = buildHeadMesh();
        MAID_HEADBAND = buildMaidHeadband();
        NEKO_EARS = buildNekoEars();
        BUNNY_EARS = buildBunnyEars();
        SUCCUBUS_HORNS = buildSuccubusHorns();

        RIGHT_THIGH_SKIN = buildThighMesh(-1, false);
        LEFT_THIGH_SKIN = buildThighMesh(+1, false);
        RIGHT_THIGH_STOCKING = buildThighMesh(-1, true);
        LEFT_THIGH_STOCKING = buildThighMesh(+1, true);

        RIGHT_SHIN_SKIN = buildShinMesh(-1, false);
        LEFT_SHIN_SKIN = buildShinMesh(+1, false);
        RIGHT_SHIN_STOCKING = buildShinMesh(-1, true);
        LEFT_SHIN_STOCKING = buildShinMesh(+1, true);

        RIGHT_UPPER_ARM = buildUpperArm(-1);
        LEFT_UPPER_ARM = buildUpperArm(+1);
        RIGHT_MAID_PUFF = buildMaidPuff(-1);
        LEFT_MAID_PUFF = buildMaidPuff(+1);
        RIGHT_FOREARM = buildForearm(-1);
        LEFT_FOREARM = buildForearm(+1);
        RIGHT_GLOVE_CUFF = buildGloveCuff(-1);
        LEFT_GLOVE_CUFF = buildGloveCuff(+1);

        WING_RIGHT_FEATHER = buildFeatherWing(-1);
        WING_LEFT_FEATHER = buildFeatherWing(+1);
        WING_RIGHT_BAT = buildBatWing(-1);
        WING_LEFT_BAT = buildBatWing(+1);

        TAIL_NEKO = buildNekoTail();
        TAIL_BUNNY = buildBunnyTail();
        TAIL_SUCCUBUS = buildSuccubusTail();
    }

    // =========================================================================
    //  Sculpted C+ Torso & Tailored Garment Shell
    // =========================================================================

    private static float[] evalTorsoPoint(float y, float theta, float pad) {
        float t = Mth.clamp((y - 0.745F) / (1.500F - 0.745F), 0.0F, 1.0F);
        float s = (float) Math.sin(theta);
        float c = (float) Math.cos(theta);

        float rx = 0.095F, rzF = 0.085F, rzB = 0.085F, zOff = 0.0F;
        if (t < 0.14F) {
            float k = t / 0.14F;
            rx = lerpf(0.118F * HIP_C, 0.116F * HIP_C, k);
            rzF = lerpf(0.088F, 0.096F, k);
            rzB = lerpf(0.096F * HIP_C, 0.116F * HIP_C, k);
            zOff = -0.008F;
        } else if (t < 0.34F) {
            float k = smoothstep(0.14F, 0.34F, t);
            rx = lerpf(0.116F * HIP_C, 0.092F, k);
            rzF = lerpf(0.096F, 0.072F, k);
            rzB = lerpf(0.116F * HIP_C, 0.082F, k);
            zOff = lerpf(-0.008F, 0.002F, k);
        } else if (t < 0.56F) {
            float k = smoothstep(0.34F, 0.56F, t);
            rx = lerpf(0.092F, 0.122F, k);
            rzF = lerpf(0.072F, 0.092F, k);
            rzB = lerpf(0.082F, 0.096F, k);
            zOff = lerpf(0.002F, 0.004F, k);
        } else if (t < 0.84F) {
            float k = smoothstep(0.56F, 0.84F, t);
            rx = lerpf(0.122F, 0.146F, k);
            rzF = lerpf(0.092F, 0.074F, k);
            rzB = lerpf(0.096F, 0.080F, k);
            zOff = lerpf(0.004F, -0.002F, k);
        } else {
            float k = smoothstep(0.84F, 1.00F, t);
            rx = lerpf(0.146F, 0.046F, k);
            rzF = lerpf(0.074F, 0.044F, k);
            rzB = lerpf(0.080F, 0.044F, k);
            zOff = lerpf(-0.002F, 0.005F, k);
        }

        float rz = (c >= 0.0F) ? rzF : rzB;
        float x = rx * s;
        float z = zOff + rz * c;

        // C+ Fullness Sculpting on Upper Front Chest
        float bustY = gaussian(y, 1.240F, 0.072F);
        float sideL = gaussian(x, -0.068F, 0.056F);
        float sideR = gaussian(x, +0.068F, 0.056F);
        float sideEnv = Math.max(sideL, sideR);
        float bustPush = 0.068F * BUST_C * bustY * sideEnv * smoothstep(-0.05F, 0.55F, c);
        float cleavage = -0.015F * BUST_C * bustY * gaussian(x, 0.0F, 0.024F) * smoothstep(0.30F, 0.85F, c);
        z += bustPush + cleavage;
        x += (sideR - sideL) * 0.013F * BUST_C * bustY * smoothstep(0.10F, 0.70F, c);

        // Anatomical Ambient Occlusion
        float underboobAO = 1.0F - 0.18F * gaussian(y, 1.158F, 0.022F) * sideEnv * smoothstep(0.0F, 0.6F, c);
        float cleavAO = 1.0F - 0.17F * bustY * gaussian(x, 0.0F, 0.020F) * smoothstep(0.25F, 0.85F, c);
        float collarAO = 1.0F - 0.08F * gaussian(y, 1.380F, 0.020F) * smoothstep(0.2F, 0.8F, c);
        float ao = underboobAO * cleavAO * collarAO;

        if (pad != 0.0F) {
            float l = (float) Math.sqrt(x * x + (z - zOff) * (z - zOff));
            if (l > 1.0E-4F) {
                x += (x / l) * pad;
                z += ((z - zOff) / l) * pad;
            }
        }
        return new float[]{x, y, z, ao};
    }

    private static QuadMesh buildTorsoShell(float pad, int tailoredMode, boolean onlyGarmentBand) {
        MeshBuilder mb = new MeshBuilder(0.0F, WAIST_PY, 0.0F);
        float y0 = onlyGarmentBand ? 0.750F : 0.745F;
        float y1 = onlyGarmentBand ? 1.340F : 1.500F;
        int R = onlyGarmentBand ? 34 : 38;
        int S = 44;

        int tileX = tailoredMode < 0 ? 0 : (tailoredMode + 1) % 4;
        int tileY = tailoredMode < 0 ? 2 : (tailoredMode + 1) / 4;

        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float vFrac = (float) r / R;
            float y = lerpf(y0, y1, vFrac);
            float vTex = tileV(tileY, tailoredMode < 0 ? vFrac : (1.0F - vFrac));
            for (int s = 0; s <= S; s++) {
                float uFrac = (float) s / S;
                float th = uFrac * TAU;
                float[] pt = evalTorsoPoint(y, th, pad);
                float ao = pt[3];
                float shade = tailoredMode < 0 ? ao : lerpf(0.86F, 1.0F, ao);
                idx[r][s] = mb.addVertex(pt[0], pt[1], pt[2], 0, 0, 1, tileU(tileX, uFrac), vTex, shade, shade, shade);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        return mb.build();
    }

    // =========================================================================
    //  Sculpted Anime Head, Face UV, Hair Locks & Headpieces
    // =========================================================================

    private static float[] evalHeadPoint(float u, float v) {
        // u in [0..TAU], v in [0..PI] bottom->top
        float sp = (float) Math.sin(v);
        float cp = (float) Math.cos(v);
        float st = (float) Math.sin(u);
        float ct = (float) Math.cos(u);

        float rx = 0.106F;
        float ry = 0.118F;
        float rz = 0.112F;
        // Taper jawline smoothly toward chin
        float jawTaper = smoothstep(-0.25F, -0.95F, cp);
        rx *= (1.0F - 0.30F * jawTaper);
        rz *= (1.0F - 0.18F * jawTaper);

        float x = rx * sp * st;
        float y = 1.606F + ry * cp;
        float z = 0.008F + rz * sp * ct;

        // Soft cheek roundness & chin point
        if (ct > 0.0F) {
            float front = ct;
            z += 0.012F * gaussian(cp, -0.35F, 0.30F) * gaussian(st, 0.0F, 0.48F) * front;
            // Subtle nose bridge
            z += 0.014F * gaussian(cp, -0.18F, 0.10F) * gaussian(st, 0.0F, 0.11F) * front;
            // Gentle eye-socket indentation
            z -= 0.005F * gaussian(cp, 0.02F, 0.16F) * (gaussian(st, -0.34F, 0.16F) + gaussian(st, 0.34F, 0.16F)) * front;
        }
        return new float[]{x, y, z};
    }

    private static void addEllipsoid(MeshBuilder mb, float cx, float cy, float cz,
                                     float rx, float ry, float rz, float rotX, float rotZ,
                                     int tx, int ty, float u0, float u1, float v0, float v1,
                                     int R, int S, float tint) {
        float sx = (float) Math.sin(rotX), cxr = (float) Math.cos(rotX);
        float sz = (float) Math.sin(rotZ), czr = (float) Math.cos(rotZ);
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float phi = PI * (1.0F - (float) r / R);
            float sp = (float) Math.sin(phi), cp = (float) Math.cos(phi);
            float v = tileV(ty, lerpf(v0, v1, 1.0F - (float) r / R));
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                float lx = rx * sp * (float) Math.sin(th);
                float ly = ry * cp;
                float lz = rz * sp * (float) Math.cos(th);
                // rotX then rotZ
                float y1 = ly * cxr - lz * sx;
                float z1 = ly * sx + lz * cxr;
                float x2 = lx * czr - y1 * sz;
                float y2 = lx * sz + y1 * czr;
                float u = tileU(tx, lerpf(u0, u1, (float) s / S));
                idx[r][s] = mb.addVertex(cx + x2, cy + y2, cz + z1, 0, 1, 0, u, v, tint, tint, tint);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
    }

    private static void addHairRibbon(MeshBuilder mb, float[] pts, float[] widths, float thick,
                                      int tx, int ty, int R, float tint) {
        // pts: (R+1)*3 coordinates along spine
        int[][] top = new int[R + 1][2];
        int[][] bot = new int[R + 1][2];
        for (int r = 0; r <= R; r++) {
            float px = pts[r * 3], py = pts[r * 3 + 1], pz = pts[r * 3 + 2];
            int rPrev = Math.max(0, r - 1), rNext = Math.min(R, r + 1);
            float txv = pts[rNext * 3] - pts[rPrev * 3];
            float tyv = pts[rNext * 3 + 1] - pts[rPrev * 3 + 1];
            float tzv = pts[rNext * 3 + 2] - pts[rPrev * 3 + 2];
            float rx = -tzv, rz = txv;
            float rl = (float) Math.sqrt(rx * rx + rz * rz);
            if (rl < 1.0E-5F) {
                rx = 1.0F;
                rz = 0.0F;
            } else {
                rx /= rl;
                rz /= rl;
            }
            // Outward normal approx = cross(tangent, side)
            float nx = tyv * rz;
            float ny = tzv * rx - txv * rz;
            float nz = -tyv * rx;
            float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (nl > 1.0E-5F) {
                nx /= nl;
                ny /= nl;
                nz /= nl;
            } else {
                ny = 1.0F;
            }
            float w = widths[r];
            float v = tileV(ty, (float) r / R);
            float u0 = tileU(tx, 0.1F), u1 = tileU(tx, 0.9F);
            top[r][0] = mb.addVertex(px - rx * w + nx * thick, py + ny * thick, pz - rz * w + nz * thick, 0, 1, 0, u0, v, tint, tint, tint);
            top[r][1] = mb.addVertex(px + rx * w + nx * thick, py + ny * thick, pz + rz * w + nz * thick, 0, 1, 0, u1, v, tint, tint, tint);
            bot[r][0] = mb.addVertex(px - rx * w - nx * thick, py - ny * thick, pz - rz * w - nz * thick, 0, -1, 0, u0, v, tint * 0.85F, tint * 0.85F, tint * 0.85F);
            bot[r][1] = mb.addVertex(px + rx * w - nx * thick, py - ny * thick, pz + rz * w - nz * thick, 0, -1, 0, u1, v, tint * 0.85F, tint * 0.85F, tint * 0.85F);
        }
        for (int r = 0; r < R; r++) {
            mb.addQuad(top[r][0], top[r][1], top[r + 1][1], top[r + 1][0]);
            mb.addQuad(bot[r][1], bot[r][0], bot[r + 1][0], bot[r + 1][1]);
            mb.addQuad(bot[r][0], top[r][0], top[r + 1][0], bot[r + 1][0]);
            mb.addQuad(top[r][1], bot[r][1], bot[r + 1][1], top[r + 1][1]);
        }
    }

    private static QuadMesh buildHeadMesh() {
        MeshBuilder mb = new MeshBuilder(0.0F, HEAD_PY, HEAD_PZ);

        // 1. Sculpted Head & Face UV
        int R = 24, S = 32;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float phi = PI * (1.0F - (float) r / R);
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                float[] p = evalHeadPoint(th, phi);
                // Planar front face projection -> Tile (0, 0), fallback skin tile (0, 2) on back
                boolean front = Math.cos(th) > 0.10F && Math.sin(phi) > 0.20F;
                float u, v;
                if (front) {
                    u = tileU(0, 0.5F + p[0] / 0.21F);
                    v = tileV(0, 0.5F - (p[1] - 1.606F) / 0.24F);
                } else {
                    u = tileU(0, 0.5F);
                    v = tileV(2, 0.5F);
                }
                float ao = 1.0F - 0.14F * smoothstep(-0.65F, -1.0F, (float) Math.cos(phi));
                idx[r][s] = mb.addVertex(p[0], p[1], p[2], 0, 0, 1, u, v, ao, ao, ao);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }

        // 2. Hair Crown Cap (Silky Pink-Crimson Tile (1, 2))
        int HR = 14, HS = 28;
        int[][] hIdx = new int[HR + 1][HS + 1];
        for (int r = 0; r <= HR; r++) {
            float phi = lerpf(PI * 0.62F, 0.0F, (float) r / HR);
            float sp = (float) Math.sin(phi), cp = (float) Math.cos(phi);
            for (int s = 0; s <= HS; s++) {
                float th = TAU * (float) s / HS;
                float ct = (float) Math.cos(th), st = (float) Math.sin(th);
                float lift = (ct > 0.25F) ? 0.025F * ct : 0.0F;
                float x = 0.118F * sp * st;
                float y = 1.616F + 0.124F * cp + lift * (1.0F - cp);
                float z = 0.002F + 0.122F * sp * ct;
                float u = tileU(1, (float) s / HS);
                float v = tileV(2, (float) r / HR);
                hIdx[r][s] = mb.addVertex(x, y, z, 0, 1, 0, u, v, 1, 1, 1);
            }
        }
        for (int r = 0; r < HR; r++) {
            for (int s = 0; s < HS; s++) {
                mb.addQuad(hIdx[r][s], hIdx[r][s + 1], hIdx[r + 1][s + 1], hIdx[r + 1][s]);
            }
        }

        // 3. Parted Sweeping Anime Bangs (clear above eyes!)
        for (int i = -4; i <= 4; i++) {
            float frac = i / 4.0F;
            float spread = frac * 0.095F;
            float xStart = spread * 0.42F;
            float xEnd = spread + (i >= 0 ? 0.026F : -0.026F);
            float yTip = (Math.abs(i) <= 1) ? 1.648F : (1.640F - Math.abs(frac) * 0.055F);
            int BR = 6;
            float[] pts = new float[(BR + 1) * 3];
            float[] wds = new float[BR + 1];
            for (int r = 0; r <= BR; r++) {
                float k = (float) r / BR;
                pts[r * 3] = lerpf(xStart, xEnd, k);
                pts[r * 3 + 1] = lerpf(1.725F, yTip, k * k * 0.65F + k * 0.35F);
                pts[r * 3 + 2] = lerpf(0.088F, 0.120F - Math.abs(frac) * 0.018F, (float) Math.sin(k * PI * 0.62F));
                wds[r] = lerpf(0.017F, 0.004F, k * k);
            }
            addHairRibbon(mb, pts, wds, 0.0045F, 1, 2, BR, 0.96F);
        }

        // 4. Side Face-Framing Locks (past shoulders)
        for (int side : new int[]{-1, 1}) {
            for (int layer = 0; layer < 2; layer++) {
                int SR = 10;
                float[] pts = new float[(SR + 1) * 3];
                float[] wds = new float[SR + 1];
                for (int r = 0; r <= SR; r++) {
                    float k = (float) r / SR;
                    float wave = (float) Math.sin(k * PI * 1.4F) * 0.012F;
                    pts[r * 3] = side * (0.106F + layer * 0.010F + k * 0.026F + wave);
                    pts[r * 3 + 1] = lerpf(1.680F, 1.240F + layer * 0.045F, k);
                    pts[r * 3 + 2] = lerpf(0.048F - layer * 0.028F, 0.078F - layer * 0.036F, k);
                    wds[r] = lerpf(0.025F, 0.006F, k * k);
                }
                addHairRibbon(mb, pts, wds, 0.005F, 1, 2, SR, 0.94F);
            }
        }

        // 5. Flowing Long Back Hair Curtain (down to upper waist)
        for (int i = -4; i <= 4; i++) {
            float frac = i / 4.0F;
            int KR = 12;
            float[] pts = new float[(KR + 1) * 3];
            float[] wds = new float[KR + 1];
            for (int r = 0; r <= KR; r++) {
                float k = (float) r / KR;
                float sway = (float) Math.sin(k * PI * 1.3F) * 0.014F;
                pts[r * 3] = frac * (0.095F + 0.045F * k);
                pts[r * 3 + 1] = lerpf(1.660F, 1.000F + Math.abs(frac) * 0.065F, k);
                pts[r * 3 + 2] = lerpf(-0.088F, -0.145F - sway, k) + Math.abs(frac) * 0.028F;
                wds[r] = lerpf(0.032F, 0.010F, k * k);
            }
            addHairRibbon(mb, pts, wds, 0.0055F, 1, 2, KR, 0.92F);
        }

        return mb.build();
    }

    private static QuadMesh buildMaidHeadband() {
        MeshBuilder mb = new MeshBuilder(0.0F, HEAD_PY, HEAD_PZ);
        int R = 8, S = 24;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float v = (float) r / R;
            float th = lerpf(-PI * 0.56F, PI * 0.56F, v);
            for (int s = 0; s <= S; s++) {
                float phi = (float) s / S * TAU;
                float w = 0.022F * (1.0F + 0.45F * (float) Math.cos(th * 6.0F));
                float x = (0.125F + 0.006F * (float) Math.cos(phi)) * (float) Math.sin(th);
                float y = 1.655F + 0.095F * (float) Math.cos(th) + 0.008F * (float) Math.sin(phi);
                float z = 0.030F + w * (float) Math.sin(phi);
                idx[r][s] = mb.addVertex(x, y, z, 0, 1, 0, tileU(3, (float) s / S), tileV(2, v * 0.45F), 1, 1, 1);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildNekoEars() {
        MeshBuilder mb = new MeshBuilder(0.0F, HEAD_PY, HEAD_PZ);
        for (int side : new int[]{-1, 1}) {
            addEllipsoid(mb, side * 0.082F, 1.745F, 0.015F, 0.032F, 0.062F, 0.024F, 0.14F, -side * 0.34F, 1, 3, 0.05F, 0.28F, 0.1F, 0.9F, 10, 14, 1.0F);
            addEllipsoid(mb, side * 0.080F, 1.740F, 0.024F, 0.019F, 0.044F, 0.012F, 0.14F, -side * 0.34F, 3, 2, 0.1F, 0.9F, 0.55F, 0.95F, 8, 12, 1.0F);
        }
        return mb.build();
    }

    private static QuadMesh buildBunnyEars() {
        MeshBuilder mb = new MeshBuilder(0.0F, HEAD_PY, HEAD_PZ);
        for (int side : new int[]{-1, 1}) {
            addEllipsoid(mb, side * 0.058F, 1.845F, 0.008F, 0.026F, 0.135F, 0.018F, -0.10F, -side * 0.18F, 1, 3, 0.36F, 0.62F, 0.1F, 0.9F, 12, 16, 1.0F);
            addEllipsoid(mb, side * 0.058F, 1.840F, 0.017F, 0.015F, 0.100F, 0.008F, -0.10F, -side * 0.18F, 3, 2, 0.1F, 0.9F, 0.55F, 0.95F, 8, 12, 1.0F);
        }
        return mb.build();
    }

    private static QuadMesh buildSuccubusHorns() {
        MeshBuilder mb = new MeshBuilder(0.0F, HEAD_PY, HEAD_PZ);
        for (int side : new int[]{-1, 1}) {
            int R = 10, S = 12;
            int[][] idx = new int[R + 1][S + 1];
            for (int r = 0; r <= R; r++) {
                float k = (float) r / R;
                float cx = side * (0.072F + k * 0.068F - k * k * 0.028F);
                float cy = 1.710F + k * 0.115F + (float) Math.sin(k * PI) * 0.025F;
                float cz = 0.015F - k * 0.058F;
                float rad = lerpf(0.022F, 0.004F, k);
                for (int s = 0; s <= S; s++) {
                    float th = TAU * (float) s / S;
                    float x = cx + rad * (float) Math.cos(th);
                    float y = cy + rad * (float) Math.sin(th) * 0.6F;
                    float z = cz + rad * (float) Math.sin(th) * 0.8F;
                    idx[r][s] = mb.addVertex(x, y, z, 0, 1, 0, tileU(0, 0.82F), tileV(3, k), 1, 1, 1);
                }
            }
            for (int r = 0; r < R; r++) {
                for (int s = 0; s < S; s++) {
                    mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
                }
            }
        }
        return mb.build();
    }

    // =========================================================================
    //  Sculpted Legs (Thighs + Shins + High Heels + Stockings & Garters)
    // =========================================================================

    private static QuadMesh buildThighMesh(int side, boolean stocking) {
        MeshBuilder mb = new MeshBuilder(side * HIP_PX, HIP_PY, HIP_PZ);
        float y0 = KNEE_PY - 0.025F, y1 = HIP_PY + 0.045F;
        int R = 18, S = 22;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(y0, y1, k);
            float radX = lerpf(0.048F, 0.076F * 1.10F, smoothstep(0.0F, 0.85F, k));
            float radZ = lerpf(0.050F, 0.082F * 1.12F, smoothstep(0.0F, 0.85F, k));
            float cx = side * lerpf(0.082F, HIP_PX, k);
            float cz = lerpf(KNEE_PZ, HIP_PZ, k);
            boolean isGarter = stocking && (y >= 0.720F && y <= 0.755F);
            boolean isStock = stocking && (y < 0.735F);
            int tx = isGarter ? 3 : (isStock ? 2 : 0);
            int ty = 2;
            float pad = isGarter ? 0.004F : (isStock ? 0.0022F : 0.0F);
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                float px = cx + (radX + pad) * (float) Math.sin(th);
                float pz = cz + (radZ + pad) * (float) Math.cos(th);
                float ao = isGarter ? 0.98F : 0.94F;
                idx[r][s] = mb.addVertex(px, y, pz, 0, 0, 1, tileU(tx, (float) s / S), tileV(ty, isGarter ? 0.25F : 0.5F), ao, ao, ao);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildShinMesh(int side, boolean stocking) {
        MeshBuilder mb = new MeshBuilder(side * HIP_PX, KNEE_PY, KNEE_PZ);
        float y0 = 0.075F, y1 = KNEE_PY + 0.025F;
        int R = 18, S = 22;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(y0, y1, k);
            // Sculpted calf curve peaking around k=0.58
            float calf = gaussian(k, 0.58F, 0.22F) * 0.015F;
            float rad = lerpf(0.032F, 0.048F, k) + calf + (stocking ? 0.002F : 0.0F);
            float cx = side * 0.082F;
            float cz = lerpf(-0.004F, KNEE_PZ, k) - calf * 0.4F;
            int tx = stocking ? 2 : 0;
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                idx[r][s] = mb.addVertex(cx + rad * (float) Math.sin(th), y, cz + rad * (float) Math.cos(th),
                        0, 0, 1, tileU(tx, (float) s / S), tileV(2, k), 0.95F, 0.95F, 0.95F);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        // High-Heel Shoe + Stiletto Heel (Tile (1,3) velvet/gloss)
        addEllipsoid(mb, side * 0.082F, 0.038F, 0.032F, 0.036F, 0.026F, 0.078F, 0.15F, 0.0F, 1, 3, 0.05F, 0.28F, 0.1F, 0.9F, 10, 14, 0.95F);
        addEllipsoid(mb, side * 0.082F, 0.036F, -0.034F, 0.010F, 0.036F, 0.010F, 0.0F, 0.0F, 1, 3, 0.05F, 0.28F, 0.1F, 0.9F, 6, 10, 0.90F);
        return mb.build();
    }

    // =========================================================================
    //  Sculpted Arms, Forearms, Hands & Cuffs
    // =========================================================================

    private static QuadMesh buildUpperArm(int side) {
        MeshBuilder mb = new MeshBuilder(side * SHLD_PX, SHLD_PY, SHLD_PZ);
        int R = 12, S = 18;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(ELBOW_PY - 0.018F, SHLD_PY + 0.024F, k);
            float rad = lerpf(0.034F, 0.044F, smoothstep(0.0F, 0.9F, k));
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                idx[r][s] = mb.addVertex(side * SHLD_PX + rad * (float) Math.sin(th), y, SHLD_PZ + rad * (float) Math.cos(th),
                        0, 0, 1, tileU(0, (float) s / S), tileV(2, k), 0.96F, 0.96F, 0.96F);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildMaidPuff(int side) {
        MeshBuilder mb = new MeshBuilder(side * SHLD_PX, SHLD_PY, SHLD_PZ);
        addEllipsoid(mb, side * SHLD_PX, SHLD_PY - 0.025F, SHLD_PZ, 0.054F, 0.054F, 0.054F, 0.0F, 0.0F, 3, 2, 0.1F, 0.9F, 0.05F, 0.45F, 10, 14, 1.0F);
        return mb.build();
    }

    private static QuadMesh buildForearm(int side) {
        MeshBuilder mb = new MeshBuilder(side * SHLD_PX, ELBOW_PY, SHLD_PZ);
        int R = 12, S = 18;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(0.835F, ELBOW_PY + 0.018F, k);
            float rad = lerpf(0.025F, 0.034F, k);
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                idx[r][s] = mb.addVertex(side * SHLD_PX + rad * (float) Math.sin(th), y, SHLD_PZ + rad * (float) Math.cos(th),
                        0, 0, 1, tileU(0, (float) s / S), tileV(2, k), 0.96F, 0.96F, 0.96F);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        // Graceful Slim Hand
        addEllipsoid(mb, side * SHLD_PX, 0.805F, SHLD_PZ + 0.008F, 0.022F, 0.044F, 0.015F, 0.12F, 0.0F, 0, 2, 0.1F, 0.9F, 0.1F, 0.9F, 8, 12, 0.96F);
        return mb.build();
    }

    private static QuadMesh buildGloveCuff(int side) {
        MeshBuilder mb = new MeshBuilder(side * SHLD_PX, ELBOW_PY, SHLD_PZ);
        addEllipsoid(mb, side * SHLD_PX, 0.858F, SHLD_PZ, 0.033F, 0.022F, 0.033F, 0.0F, 0.0F, 3, 2, 0.1F, 0.9F, 0.05F, 0.45F, 8, 12, 1.0F);
        return mb.build();
    }

    // =========================================================================
    //  Outfit Skirts, Trims, Wings & Tails
    // =========================================================================

    private static QuadMesh buildBikiniTrims() {
        MeshBuilder mb = new MeshBuilder(0.0F, WAIST_PY, 0.0F);
        // Gold Neck Halter Straps
        for (int side : new int[]{-1, 1}) {
            int R = 8;
            float[] pts = new float[(R + 1) * 3];
            float[] wds = new float[R + 1];
            for (int r = 0; r <= R; r++) {
                float k = (float) r / R;
                pts[r * 3] = side * lerpf(0.070F, 0.045F, k);
                pts[r * 3 + 1] = lerpf(1.300F, 1.440F, k);
                pts[r * 3 + 2] = lerpf(0.118F, 0.025F, k) + (float) Math.sin(k * PI) * 0.015F;
                wds[r] = 0.006F;
            }
            addHairRibbon(mb, pts, wds, 0.003F, 0, 3, R, 1.0F);
        }
        return mb.build();
    }

    private static QuadMesh buildEveningSkirt() {
        MeshBuilder mb = new MeshBuilder(0.0F, WAIST_PY, 0.0F);
        int R = 16, S = 38;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(0.925F, 0.340F, k);
            for (int s = 0; s <= S; s++) {
                // High slit on right leg (-X / front-right)
                float th = lerpf(-0.18F * PI, 1.56F * PI, (float) s / S);
                float[] base = evalTorsoPoint(Math.max(y, 0.765F), th, 0.008F);
                float flare = 1.0F + k * 0.62F;
                float px = base[0] * flare;
                float pz = (base[2] - 0.005F) * flare + 0.005F;
                float pleat = 1.0F + 0.020F * k * (float) Math.sin(th * 12.0F);
                idx[r][s] = mb.addVertex(px * pleat, y, pz * pleat, 0, 0, 1,
                        tileU(1, lerpf(0.68F, 0.96F, (float) s / S)), tileV(3, k), 0.96F, 0.96F, 0.96F);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
                mb.addQuad(idx[r + 1][s], idx[r + 1][s + 1], idx[r][s + 1], idx[r][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildMaidSkirt() {
        MeshBuilder mb = new MeshBuilder(0.0F, WAIST_PY, 0.0F);
        int R = 14, S = 48;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(0.965F, 0.635F, k);
            float pleatAmp = 0.022F * k;
            int tx = (k > 0.84F) ? 3 : 1;
            int ty = (k > 0.84F) ? 2 : 3;
            for (int s = 0; s <= S; s++) {
                float u = (float) s / S;
                float th = u * TAU;
                float rx = lerpf(0.118F, 0.285F, (float) Math.pow(k, 0.78F)) + pleatAmp * (float) Math.cos(th * 16.0F);
                float rz = lerpf(0.095F, 0.245F, (float) Math.pow(k, 0.78F)) + pleatAmp * (float) Math.cos(th * 16.0F);
                float uTex = (k > 0.84F) ? tileU(tx, u) : tileU(tx, lerpf(0.04F, 0.30F, u));
                float vTex = (k > 0.84F) ? tileV(ty, (k - 0.84F) / 0.16F * 0.45F) : tileV(ty, k);
                idx[r][s] = mb.addVertex(rx * (float) Math.sin(th), y, -0.006F + rz * (float) Math.cos(th),
                        0, 0, 1, uTex, vTex, 0.96F, 0.96F, 0.96F);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
                mb.addQuad(idx[r + 1][s], idx[r + 1][s + 1], idx[r][s + 1], idx[r][s]);
            }
        }
        // White Front Lace Apron
        int AR = 12, AS = 18;
        int[][] aIdx = new int[AR + 1][AS + 1];
        for (int r = 0; r <= AR; r++) {
            float k = (float) r / AR;
            float y = lerpf(0.965F, 0.655F, k);
            for (int s = 0; s <= AS; s++) {
                float th = lerpf(-0.32F * PI, 0.32F * PI, (float) s / AS);
                float rx = lerpf(0.125F, 0.298F, (float) Math.pow(k, 0.78F));
                float rz = lerpf(0.102F, 0.258F, (float) Math.pow(k, 0.78F));
                aIdx[r][s] = mb.addVertex(rx * (float) Math.sin(th), y, -0.006F + rz * (float) Math.cos(th),
                        0, 0, 1, tileU(3, (float) s / AS), tileV(2, k * 0.45F), 1.0F, 1.0F, 1.0F);
            }
        }
        for (int r = 0; r < AR; r++) {
            for (int s = 0; s < AS; s++) {
                mb.addQuad(aIdx[r][s], aIdx[r][s + 1], aIdx[r + 1][s + 1], aIdx[r + 1][s]);
                mb.addQuad(aIdx[r + 1][s], aIdx[r + 1][s + 1], aIdx[r][s + 1], aIdx[r][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildNegligeeChiffon() {
        MeshBuilder mb = new MeshBuilder(0.0F, WAIST_PY, 0.0F);
        int R = 16, S = 42;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float y = lerpf(1.168F, 0.680F, k);
            for (int s = 0; s <= S; s++) {
                float u = (float) s / S;
                float th = u * TAU;
                float[] base = evalTorsoPoint(Math.max(y, 0.765F), th, 0.010F);
                float flare = 1.0F + k * 0.56F;
                float wave = 1.0F + 0.025F * k * (float) Math.sin(th * 14.0F);
                idx[r][s] = mb.addVertex(base[0] * flare * wave, y, base[2] * flare * wave,
                        0, 0, 1, tileU(3, u), tileV(3, k), 1.0F, 1.0F, 1.0F);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
                mb.addQuad(idx[r + 1][s], idx[r + 1][s + 1], idx[r][s + 1], idx[r][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildFeatherWing(int side) {
        MeshBuilder mb = new MeshBuilder(side * WING_PX, WING_PY, WING_PZ);
        for (int i = 0; i < 6; i++) {
            float f = i / 5.0F;
            float cx = side * (0.14F + f * 0.24F);
            float cy = 1.38F - f * 0.16F;
            float cz = -0.15F - f * 0.06F;
            addEllipsoid(mb, cx, cy, cz, 0.085F - f * 0.020F, 0.024F, 0.010F,
                    0.15F, side * (-0.35F + f * 0.55F), 2, 3, 0.05F, 0.45F, 0.1F, 0.9F, 6, 10, 1.0F);
        }
        return mb.build();
    }

    private static QuadMesh buildBatWing(int side) {
        MeshBuilder mb = new MeshBuilder(side * WING_PX, WING_PY, WING_PZ);
        int R = 8, S = 12;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float u = (float) r / R;
            for (int s = 0; s <= S; s++) {
                float v = (float) s / S;
                float x = side * (0.06F + u * 0.34F);
                float scallop = (float) Math.sin(u * PI * 3.0F) * 0.025F * v;
                float y = lerpf(1.30F + u * 0.14F, 1.06F + u * 0.06F + scallop, v);
                float z = -0.09F - u * 0.15F - (float) Math.sin(u * PI) * 0.04F;
                idx[r][s] = mb.addVertex(x, y, z, 0, 0, -1, tileU(2, lerpf(0.54F, 0.96F, u)), tileV(3, v), 1, 1, 1);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
                mb.addQuad(idx[r + 1][s], idx[r + 1][s + 1], idx[r][s + 1], idx[r][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildNekoTail() {
        MeshBuilder mb = new MeshBuilder(0.0F, TAIL_PY, TAIL_PZ);
        int R = 14, S = 12;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float cx = (float) Math.sin(k * PI * 1.4F) * 0.095F;
            float cy = TAIL_PY - 0.08F * (float) Math.sin(k * PI) + k * 0.24F;
            float cz = TAIL_PZ - 0.05F - k * 0.26F;
            float rad = lerpf(0.017F, 0.011F, k);
            int tx = (k > 0.80F) ? 3 : 1;
            int ty = (k > 0.80F) ? 2 : 3;
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                float uTex = (k > 0.80F) ? tileU(tx, (float) s / S) : tileU(tx, lerpf(0.05F, 0.28F, (float) s / S));
                float vTex = (k > 0.80F) ? tileV(ty, 0.75F) : tileV(ty, k);
                idx[r][s] = mb.addVertex(cx + rad * (float) Math.cos(th), cy + rad * (float) Math.sin(th), cz,
                        0, 1, 0, uTex, vTex, 1, 1, 1);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        return mb.build();
    }

    private static QuadMesh buildBunnyTail() {
        MeshBuilder mb = new MeshBuilder(0.0F, TAIL_PY, TAIL_PZ);
        addEllipsoid(mb, 0.0F, TAIL_PY + 0.02F, TAIL_PZ - 0.028F, 0.052F, 0.052F, 0.052F, 0, 0, 3, 2, 0.1F, 0.9F, 0.05F, 0.45F, 10, 14, 1.0F);
        return mb.build();
    }

    private static QuadMesh buildSuccubusTail() {
        MeshBuilder mb = new MeshBuilder(0.0F, TAIL_PY, TAIL_PZ);
        int R = 14, S = 10;
        int[][] idx = new int[R + 1][S + 1];
        for (int r = 0; r <= R; r++) {
            float k = (float) r / R;
            float cx = (float) Math.sin(k * PI * 1.6F) * 0.12F;
            float cy = TAIL_PY - 0.05F * (float) Math.sin(k * PI) + k * 0.28F;
            float cz = TAIL_PZ - 0.04F - k * 0.28F;
            float rad = (k > 0.88F) ? 0.028F * (1.0F - (k - 0.88F) / 0.12F) : 0.008F;
            for (int s = 0; s <= S; s++) {
                float th = TAU * (float) s / S;
                idx[r][s] = mb.addVertex(cx + rad * (float) Math.cos(th), cy + rad * (float) Math.sin(th), cz,
                        0, 1, 0, tileU(0, 0.82F), tileV(3, k), 1, 1, 1);
            }
        }
        for (int r = 0; r < R; r++) {
            for (int s = 0; s < S; s++) {
                mb.addQuad(idx[r][s], idx[r][s + 1], idx[r + 1][s + 1], idx[r + 1][s]);
            }
        }
        return mb.build();
    }

    // =========================================================================
    //  Animated Pose Evaluation & Submission
    // =========================================================================

    private static void submitMesh(PoseStack matrices, SubmitNodeCollector collector, RenderType type,
                                   QuadMesh mesh, int light, float alpha, float r, float g, float b) {
        collector.submitCustomGeometry(matrices, type, (pose, consumer) -> mesh.emit(pose, consumer, light, alpha, r, g, b));
    }

    public static void render(PoseStack matrices, SubmitNodeCollector collector, int light,
                              GolemRenderState state, float limbAngle, float limbDistance) {
        float age = state.ageInTicks;
        float walk = Mth.sin(limbAngle * 0.6662F) * limbDistance;
        float walkCos = Mth.cos(limbAngle * 0.6662F) * limbDistance;

        float t = state.interactionTicks;
        int anim = state.interactionAnim;

        // Skeletal parameters
        float pelY = 0.0F, pelPitch = 0.0F, pelYaw = 0.0F, pelRoll = 0.0F;
        float waistPitch = Mth.sin(age * 0.08F) * 0.018F, waistYaw = 0.0F, waistRoll = 0.0F;
        float headPitch = state.xRot * Mth.DEG_TO_RAD;
        float headYaw = -state.yRot * Mth.DEG_TO_RAD;
        float headRoll = 0.0F;

        float rArmX = -0.05F - walk * 0.45F, rArmY = 0.0F, rArmZ = -0.18F;
        float lArmX = -0.05F + walk * 0.45F, lArmY = 0.0F, lArmZ = +0.18F;
        float rForeX = -0.14F, lForeX = -0.14F;

        float rLegX = +walk * 0.55F, rLegZ = -0.025F, rShinX = Math.max(0.0F, +walkCos * 0.45F);
        float lLegX = -walk * 0.55F, lLegZ = +0.025F, lShinX = Math.max(0.0F, -walkCos * 0.45F);
        float wingFlap = Mth.sin(age * 0.10F) * 0.10F;
        float tailWag = Mth.sin(age * 0.14F) * 0.24F;

        // Sitting / OP Pose Overrides (for -Y limbs around +X: negative = forward, positive = backward)
        if (state.isSitting || anim == ValkyrieGolemEntity.ANIM_ADMIN_SIT) {
            pelY = -0.45F;
            rLegX = -1.38F;
            lLegX = -1.38F;
            rShinX = +1.45F;
            lShinX = +1.45F;
        }

        if (anim != ValkyrieGolemEntity.ANIM_NONE) {
            switch (anim) {
                case ValkyrieGolemEntity.ANIM_OP_FLIRT -> {
                    float s = Mth.sin(Math.min(1.0F, t / 25.0F) * PI * 0.5F);
                    pelRoll = 0.08F * s;
                    headRoll = -0.22F * s;
                    rArmX = -1.35F * s;
                    rArmZ = -0.45F * s;
                    rForeX = -1.35F * s;
                    lArmZ = +0.28F;
                }
                case ValkyrieGolemEntity.ANIM_OP_NYA -> {
                    float b = Mth.sin(t * 0.25F);
                    pelY = Math.abs(b) * 0.03F;
                    headRoll = 0.18F * b;
                    rArmX = -1.15F;
                    lArmX = -1.15F;
                    rForeX = -1.30F;
                    lForeX = -1.30F;
                    tailWag = Mth.sin(t * 0.45F) * 0.55F;
                }
                case ValkyrieGolemEntity.ANIM_OP_SEDUCTIVE_DANCE -> {
                    pelYaw = Mth.sin(t * 0.12F) * 0.35F;
                    pelRoll = Mth.sin(t * 0.24F) * 0.12F;
                    waistRoll = -pelRoll * 0.85F;
                    rArmX = -1.4F - Mth.sin(t * 0.2F) * 0.4F;
                    lArmX = -1.4F + Mth.sin(t * 0.2F) * 0.4F;
                    rArmZ = -0.6F;
                    lArmZ = +0.6F;
                }
                case ValkyrieGolemEntity.ANIM_OP_CUDDLE, ValkyrieGolemEntity.ANIM_HUG -> {
                    waistPitch = 0.14F;
                    rArmX = -1.25F;
                    lArmX = -1.25F;
                    rArmY = -0.42F;
                    lArmY = +0.42F;
                    rForeX = -0.65F;
                    lForeX = -0.65F;
                }
                case ValkyrieGolemEntity.ANIM_OP_HEART -> {
                    rArmX = -1.20F;
                    lArmX = -1.20F;
                    rArmZ = -0.55F;
                    lArmZ = +0.55F;
                    rForeX = -1.45F;
                    lForeX = -1.45F;
                    headRoll = 0.12F;
                }
                case ValkyrieGolemEntity.ANIM_OP_MAID_BOW, ValkyrieGolemEntity.ANIM_BOW -> {
                    float b = Mth.sin(Math.min(1.0F, t / 30.0F) * PI);
                    waistPitch = 0.42F * b;
                    rArmZ = -0.45F * b;
                    lArmZ = +0.45F * b;
                }
                case ValkyrieGolemEntity.ANIM_OP_LOUNGE, ValkyrieGolemEntity.ANIM_LIE -> {
                    pelY = -0.62F;
                    pelPitch = -1.25F;
                    headPitch = 0.85F;
                    rLegX = -0.35F;
                    lLegX = -0.15F;
                }
                case ValkyrieGolemEntity.ANIM_OP_KNEEL -> {
                    pelY = -0.44F;
                    rLegX = -1.30F;
                    lLegX = -1.30F;
                    rShinX = +2.35F;
                    lShinX = +2.35F;
                    waistPitch = 0.18F;
                    headPitch = -0.35F;
                    rArmX = -0.85F;
                    lArmX = -0.85F;
                    rArmY = -0.35F;
                    lArmY = +0.35F;
                }
                case ValkyrieGolemEntity.ANIM_OP_STRETCH -> {
                    float s = Mth.sin(Math.min(1.0F, t / 40.0F) * PI);
                    waistPitch = -0.22F * s;
                    headPitch = -0.20F * s;
                    rArmZ = -2.65F * s - 0.18F;
                    lArmZ = +2.65F * s + 0.18F;
                    rForeX = -0.35F * s;
                    lForeX = -0.35F * s;
                }
                case ValkyrieGolemEntity.ANIM_OP_TWIRL -> {
                    float spin = Math.min(1.0F, t / 42.0F) * TAU;
                    pelYaw = spin;
                    rArmZ = -0.85F;
                    lArmZ = +0.85F;
                    headRoll = 0.15F;
                }
                case ValkyrieGolemEntity.ANIM_OP_TEASE -> {
                    waistPitch = 0.24F;
                    headPitch = -0.22F;
                    headRoll = -0.18F;
                    rArmX = +0.42F;
                    lArmX = +0.42F;
                    rArmY = +0.35F;
                    lArmY = -0.35F;
                }
                case ValkyrieGolemEntity.ANIM_OP_SHRUG_SHY, ValkyrieGolemEntity.ANIM_SHY -> {
                    float sw = Mth.sin(t * 0.18F) * 0.08F;
                    pelRoll = sw;
                    headPitch = 0.16F;
                    headRoll = 0.15F;
                    rArmX = -1.15F;
                    lArmX = -1.15F;
                    rForeX = -1.50F;
                    lForeX = -1.50F;
                }
                default -> {}
            }
        }

        int outfit = state.opOutfit;
        boolean hasStockings = (outfit == ValkyrieGolemEntity.OP_OUTFIT_MAID
                || outfit == ValkyrieGolemEntity.OP_OUTFIT_CAT
                || outfit == ValkyrieGolemEntity.OP_OUTFIT_EVENING
                || outfit == ValkyrieGolemEntity.OP_OUTFIT_BUNNY
                || outfit == ValkyrieGolemEntity.OP_OUTFIT_SUCCUBUS);

        int tailoredIdx = switch (outfit) {
            case ValkyrieGolemEntity.OP_OUTFIT_BIKINI, ValkyrieGolemEntity.OP_OUTFIT_SWIMSUIT -> 0;
            case ValkyrieGolemEntity.OP_OUTFIT_EVENING -> 1;
            case ValkyrieGolemEntity.OP_OUTFIT_MAID -> 2;
            case ValkyrieGolemEntity.OP_OUTFIT_CAT -> 3;
            case ValkyrieGolemEntity.OP_OUTFIT_BUNNY -> 4;
            case ValkyrieGolemEntity.OP_OUTFIT_SUCCUBUS -> 5;
            case ValkyrieGolemEntity.OP_OUTFIT_NEGLIGEE -> 6;
            default -> 0;
        };

        RenderType cutout = RenderTypes.entityCutoutNoCull(SMOOTH_ATLAS);
        RenderType translucent = RenderTypes.entityTranslucent(SMOOTH_ATLAS);

        matrices.pushPose();
        matrices.translate(0.0F, PELVIS_PY + pelY, 0.0F);
        if (pelYaw != 0.0F) matrices.mulPose(Axis.YP.rotation(pelYaw));
        if (pelPitch != 0.0F) matrices.mulPose(Axis.XP.rotation(pelPitch));
        if (pelRoll != 0.0F) matrices.mulPose(Axis.ZP.rotation(pelRoll));

        // --- Legs ---
        submitLeg(matrices, collector, cutout, light, -1, hasStockings, rLegX, rLegZ, rShinX);
        submitLeg(matrices, collector, cutout, light, +1, hasStockings, lLegX, lLegZ, lShinX);

        // --- Upper Body / Waist Pivot ---
        matrices.pushPose();
        matrices.translate(0.0F, WAIST_PY - PELVIS_PY, 0.0F);
        if (waistYaw != 0.0F) matrices.mulPose(Axis.YP.rotation(waistYaw));
        if (waistPitch != 0.0F) matrices.mulPose(Axis.XP.rotation(waistPitch));
        if (waistRoll != 0.0F) matrices.mulPose(Axis.ZP.rotation(waistRoll));

        // Torso + Tailored Garment + Skirts
        submitMesh(matrices, collector, cutout, TORSO_BASE, light, 1.0F, 1.0F, 1.0F, 1.0F);
        submitMesh(matrices, collector, cutout, TAILORED_OUTFITS[tailoredIdx], light, 1.0F, 1.0F, 1.0F, 1.0F);
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_BIKINI || outfit == ValkyrieGolemEntity.OP_OUTFIT_SWIMSUIT) {
            submitMesh(matrices, collector, cutout, BIKINI_TRIMS, light, 1.0F, 1.0F, 1.0F, 1.0F);
        } else if (outfit == ValkyrieGolemEntity.OP_OUTFIT_EVENING) {
            submitMesh(matrices, collector, cutout, EVENING_SKIRT, light, 1.0F, 1.0F, 1.0F, 1.0F);
        } else if (outfit == ValkyrieGolemEntity.OP_OUTFIT_MAID) {
            submitMesh(matrices, collector, cutout, MAID_SKIRT, light, 1.0F, 1.0F, 1.0F, 1.0F);
        } else if (outfit == ValkyrieGolemEntity.OP_OUTFIT_NEGLIGEE) {
            submitMesh(matrices, collector, translucent, NEGLIGEE_CHIFFON, light, 0.72F, 1.0F, 1.0F, 1.0F);
        }

        // Tail
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_CAT || state.opCatEars || outfit == ValkyrieGolemEntity.OP_OUTFIT_BUNNY || outfit == ValkyrieGolemEntity.OP_OUTFIT_SUCCUBUS) {
            matrices.pushPose();
            matrices.translate(0.0F, TAIL_PY - WAIST_PY, TAIL_PZ);
            matrices.mulPose(Axis.YP.rotation(tailWag));
            QuadMesh tail = outfit == ValkyrieGolemEntity.OP_OUTFIT_BUNNY ? TAIL_BUNNY
                    : (outfit == ValkyrieGolemEntity.OP_OUTFIT_SUCCUBUS ? TAIL_SUCCUBUS : TAIL_NEKO);
            submitMesh(matrices, collector, cutout, tail, light, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.popPose();
        }

        // Wings (Bat wings for Succubus, Feathered Valkyrie wings otherwise)
        boolean batWings = (outfit == ValkyrieGolemEntity.OP_OUTFIT_SUCCUBUS);
        for (int side : new int[]{-1, 1}) {
            matrices.pushPose();
            matrices.translate(side * WING_PX, WING_PY - WAIST_PY, WING_PZ);
            matrices.mulPose(Axis.YP.rotation(-side * wingFlap));
            QuadMesh w = batWings ? (side < 0 ? WING_RIGHT_BAT : WING_LEFT_BAT) : (side < 0 ? WING_RIGHT_FEATHER : WING_LEFT_FEATHER);
            submitMesh(matrices, collector, cutout, w, light, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.popPose();
        }

        // Head + Headpieces
        matrices.pushPose();
        matrices.translate(0.0F, HEAD_PY - WAIST_PY, HEAD_PZ);
        if (headYaw != 0.0F) matrices.mulPose(Axis.YP.rotation(headYaw));
        if (headPitch != 0.0F) matrices.mulPose(Axis.XP.rotation(headPitch));
        if (headRoll != 0.0F) matrices.mulPose(Axis.ZP.rotation(headRoll));
        submitMesh(matrices, collector, cutout, HEAD_MESH, light, 1.0F, 1.0F, 1.0F, 1.0F);
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_MAID) {
            submitMesh(matrices, collector, cutout, MAID_HEADBAND, light, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_CAT || state.opCatEars) {
            submitMesh(matrices, collector, cutout, NEKO_EARS, light, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_BUNNY) {
            submitMesh(matrices, collector, cutout, BUNNY_EARS, light, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_SUCCUBUS) {
            submitMesh(matrices, collector, cutout, SUCCUBUS_HORNS, light, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        matrices.popPose();

        // Arms
        submitArm(matrices, collector, cutout, light, -1, outfit, rArmX, rArmY, rArmZ, rForeX);
        submitArm(matrices, collector, cutout, light, +1, outfit, lArmX, lArmY, lArmZ, lForeX);

        matrices.popPose(); // waist
        matrices.popPose(); // pelvis
    }

    private static void submitLeg(PoseStack matrices, SubmitNodeCollector collector, RenderType type,
                                  int light, int side, boolean stockings,
                                  float legX, float legZ, float shinX) {
        matrices.pushPose();
        matrices.translate(side * HIP_PX, HIP_PY - PELVIS_PY, HIP_PZ);
        if (legX != 0.0F) matrices.mulPose(Axis.XP.rotation(legX));
        if (legZ != 0.0F) matrices.mulPose(Axis.ZP.rotation(legZ));
        QuadMesh thigh = side < 0 ? (stockings ? RIGHT_THIGH_STOCKING : RIGHT_THIGH_SKIN)
                : (stockings ? LEFT_THIGH_STOCKING : LEFT_THIGH_SKIN);
        submitMesh(matrices, collector, type, thigh, light, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.translate(0.0F, KNEE_PY - HIP_PY, KNEE_PZ - HIP_PZ);
        if (shinX != 0.0F) matrices.mulPose(Axis.XP.rotation(shinX));
        QuadMesh shin = side < 0 ? (stockings ? RIGHT_SHIN_STOCKING : RIGHT_SHIN_SKIN)
                : (stockings ? LEFT_SHIN_STOCKING : LEFT_SHIN_SKIN);
        submitMesh(matrices, collector, type, shin, light, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.popPose();
    }

    private static void submitArm(PoseStack matrices, SubmitNodeCollector collector, RenderType type,
                                  int light, int side, int outfit,
                                  float armX, float armY, float armZ, float foreX) {
        matrices.pushPose();
        matrices.translate(side * SHLD_PX, SHLD_PY - WAIST_PY, SHLD_PZ);
        if (armY != 0.0F) matrices.mulPose(Axis.YP.rotation(armY));
        if (armX != 0.0F) matrices.mulPose(Axis.XP.rotation(armX));
        if (armZ != 0.0F) matrices.mulPose(Axis.ZP.rotation(armZ));
        submitMesh(matrices, collector, type, side < 0 ? RIGHT_UPPER_ARM : LEFT_UPPER_ARM, light, 1.0F, 1.0F, 1.0F, 1.0F);
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_MAID) {
            submitMesh(matrices, collector, type, side < 0 ? RIGHT_MAID_PUFF : LEFT_MAID_PUFF, light, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        matrices.translate(0.0F, ELBOW_PY - SHLD_PY, 0.0F);
        if (foreX != 0.0F) matrices.mulPose(Axis.XP.rotation(foreX));
        submitMesh(matrices, collector, type, side < 0 ? RIGHT_FOREARM : LEFT_FOREARM, light, 1.0F, 1.0F, 1.0F, 1.0F);
        if (outfit == ValkyrieGolemEntity.OP_OUTFIT_MAID || outfit == ValkyrieGolemEntity.OP_OUTFIT_BUNNY) {
            submitMesh(matrices, collector, type, side < 0 ? RIGHT_GLOVE_CUFF : LEFT_GLOVE_CUFF, light, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        matrices.popPose();
    }
}
