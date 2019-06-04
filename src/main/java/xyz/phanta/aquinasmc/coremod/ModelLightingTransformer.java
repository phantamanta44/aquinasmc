package xyz.phanta.aquinasmc.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import static xyz.phanta.aquinasmc.coremod.TransNames.*;

public class ModelLightingTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        if (transformedName.equals("net.minecraft.client.renderer.ItemRenderer")) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, 0);
            reader.accept(new ClassVisitorItemRenderer(writer), 0);
            return writer.toByteArray();
        }
        return code;
    }

    private static void injectCode(MethodVisitor writer) {
        writer.visitVarInsn(Opcodes.ALOAD, 2);
        writer.visitMethodInsn(Opcodes.INVOKESTATIC,
                C_ModelLightingHandler, MN_ModelLightingHandler_itemRenderHook, MD_ModelLightingHandler_itemRenderHook, false);
        System.out.println("Successfully injected model lighting hook!");
    }

    private static class ClassVisitorItemRenderer extends ClassVisitor {

        private boolean done = false;

        ClassVisitorItemRenderer(ClassWriter cw) {
            super(Opcodes.ASM5, cw);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!done && (name.equals("renderItemSide") || name.equals("func_187462_a"))) {
                done = true;
                return new MethodVisitorRenderItemSide(super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class MethodVisitorRenderItemSide extends MethodVisitor {

        private boolean done = false;

        MethodVisitorRenderItemSide(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (!done && opcode == Opcodes.INVOKESTATIC && (name.equals("pushMatrix") || name.equals("func_179094_E"))) {
                injectCode(mv);
                done = true;
            }
        }

    }

}
