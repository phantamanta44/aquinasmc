package xyz.phanta.aquinasmc.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import static xyz.phanta.aquinasmc.coremod.TransNames.*;

public class PlayerDiggingTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        if (transformedName.equals("net.minecraft.network.NetHandlerPlayServer")) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            reader.accept(new ClassVisitorNetHandlerPlayServer(writer), 0);
            return writer.toByteArray();
        }
        return code;
    }

    private static void injectCode(MethodVisitor writer) {
        writer.visitFieldInsn(Opcodes.GETSTATIC, C_MinecraftForge, FN_MinecraftForge_EVENT_BUS, FD_MinecraftForge_EVENT_BUS);
        writer.visitTypeInsn(Opcodes.NEW, C_PlayerDiggingEvent);
        writer.visitInsn(Opcodes.DUP);
        writer.visitVarInsn(Opcodes.ALOAD, 0);
        writer.visitFieldInsn(Opcodes.GETFIELD, C_NetHandlerPlayServer, FN_NetHandlerPlayServer_player, FD_NetHandlerPlayServer_player);
        writer.visitVarInsn(Opcodes.ALOAD, 1);
        writer.visitMethodInsn(Opcodes.INVOKESPECIAL, C_PlayerDiggingEvent, "<init>", MD_PlayerDiggingEvent_new, false);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, C_EventBus, MN_EventBus_post, MD_EventBus_post, false);
        Label notCancelled = new Label();
        writer.visitJumpInsn(Opcodes.IFEQ, notCancelled);
        writer.visitInsn(Opcodes.RETURN);
        writer.visitLabel(notCancelled);
        System.out.println("Successfully injected player digging event!");
    }

    private static class ClassVisitorNetHandlerPlayServer extends ClassVisitor {

        private boolean done = false;

        ClassVisitorNetHandlerPlayServer(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!done && (name.equals("processPlayerDigging") || name.equals("func_147345_a"))) {
                done = true;
                return new MethodVisitorProcessPlayerDigging(super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class MethodVisitorProcessPlayerDigging extends MethodVisitor {

        private boolean done = false;

        MethodVisitorProcessPlayerDigging(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (!done && opcode == Opcodes.INVOKEVIRTUAL && (name.equals("markPlayerActive") || name.equals("func_143004_u"))) {
                injectCode(mv);
                done = true;
            }
        }

    }

}
