package xyz.phanta.aquinasmc.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import static xyz.phanta.aquinasmc.coremod.TransNames.*;

public class SlotClickTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        if (transformedName.equals("net.minecraft.inventory.Container")) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, 0);
            reader.accept(new ClassVisitorContainer(writer), 0);
            return writer.toByteArray();
        }
        return code;
    }

    private static void injectCode(MethodVisitor writer) {
        writer.visitFieldInsn(Opcodes.GETSTATIC, C_MinecraftForge, FN_MinecraftForge_EVENT_BUS, FD_MinecraftForge_EVENT_BUS);
        writer.visitTypeInsn(Opcodes.NEW, C_SlotClickEvent);
        writer.visitInsn(Opcodes.DUP);
        writer.visitVarInsn(Opcodes.ALOAD, 4);
        writer.visitVarInsn(Opcodes.ALOAD, 0);
        writer.visitVarInsn(Opcodes.ALOAD, 3);
        writer.visitVarInsn(Opcodes.ILOAD, 1);
        writer.visitVarInsn(Opcodes.ILOAD, 2);
        writer.visitMethodInsn(Opcodes.INVOKESPECIAL, C_SlotClickEvent, "<init>", MD_SlotClickEvent_new, false);
        writer.visitInsn(Opcodes.DUP_X1);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, C_EventBus, MN_EventBus_post, MD_EventBus_post, false);
        Label notCancelled = new Label();
        writer.visitJumpInsn(Opcodes.IFEQ, notCancelled);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                C_SlotClickEvent, MN_SlotClickEvent_getResultStack, MD_SlotClickEvent_getResultStack, false);
        writer.visitInsn(Opcodes.ARETURN);
        writer.visitLabel(notCancelled);
        writer.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { C_SlotClickEvent });
        writer.visitInsn(Opcodes.POP);
        System.out.println("Successfully injected slot click event!");
    }

    private static class ClassVisitorContainer extends ClassVisitor {

        private boolean done = false;

        ClassVisitorContainer(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!done && (name.equals("slotClick") || name.equals("func_184996_a"))) {
                done = true;
                return new MethodVisitorSlotClick(super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class MethodVisitorSlotClick extends MethodVisitor {

        MethodVisitorSlotClick(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(Math.max(maxStack, 9), maxLocals);
        }

        @Override
        public void visitCode() {
            injectCode(mv);
            super.visitCode();
        }

    }

}
