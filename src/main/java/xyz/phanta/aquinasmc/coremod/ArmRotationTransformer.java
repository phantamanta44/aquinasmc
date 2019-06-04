package xyz.phanta.aquinasmc.coremod;

import jdk.internal.org.objectweb.asm.Opcodes;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import static xyz.phanta.aquinasmc.coremod.TransNames.*;

public class ArmRotationTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        if (transformedName.equals("net.minecraft.client.renderer.entity.RenderPlayer")) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            reader.accept(new ClassVisitorRenderPlayer(writer), 0);
            return writer.toByteArray();
        }
        return code;
    }

    private static void injectCode(MethodVisitor writer, int lvStack, int lvArmPose) {
        writer.visitVarInsn(Opcodes.ALOAD, lvStack);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, C_ItemStack, MN_ItemStack_getItem, MD_ItemStack_getItem, false);
        writer.visitInsn(Opcodes.DUP);
        writer.visitTypeInsn(Opcodes.INSTANCEOF, C_ArmRotator);
        Label notArmRotator = new Label();
        writer.visitJumpInsn(Opcodes.IFEQ, notArmRotator);
        writer.visitVarInsn(Opcodes.ALOAD, 1);
        writer.visitVarInsn(Opcodes.ALOAD, lvStack);
        writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, C_ArmRotator, MN_ArmRotator_getArmPose, MD_ArmRotator_getArmPose, true);
        writer.visitVarInsn(Opcodes.ASTORE, lvArmPose);
        Label isArmRotator = new Label();
        writer.visitJumpInsn(Opcodes.GOTO, isArmRotator);
        writer.visitLabel(notArmRotator);
        writer.visitInsn(Opcodes.POP);
        writer.visitLabel(isArmRotator);
    }

    private static class ClassVisitorRenderPlayer extends ClassVisitor {

        private boolean done = false;

        ClassVisitorRenderPlayer(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!done && (name.equals("setModelVisibilities") || name.equals("func_177137_d"))) {
                done = true;
                return new MethodVisitorSetModelVisibilities(super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class MethodVisitorSetModelVisibilities extends MethodVisitor {

        private int state = 0;
        private int lvStack = -1;

        MethodVisitorSetModelVisibilities(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            if (state == 2) {
                injectCode(mv, lvStack, var);
                state = 0;
            } else if (state == 0 && opcode == Opcodes.ALOAD) {
                lvStack = var;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (state == 0 && opcode == Opcodes.INVOKEVIRTUAL && owner.equals(C_ItemStack)
                    && (name.equals("isEmpty") || name.equals("func_190926_b"))) {
                state = 1;
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (state == 1 && opcode == Opcodes.GETSTATIC && owner.equals(C_ArmPose) && name.equals("ITEM")) {
                state = 2;
            }
        }

    }

}
