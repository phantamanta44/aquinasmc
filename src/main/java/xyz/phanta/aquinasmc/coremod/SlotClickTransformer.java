package xyz.phanta.aquinasmc.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class SlotClickTransformer implements IClassTransformer {

    private static final String CLNAME_EPLAYER = "net/minecraft/entity/player/EntityPlayer";
    private static final String CLNAME_CONT = "net/minecraft/inventory/Container";
    private static final String CLNAME_CLICK = "net/minecraft/inventory/ClickType";

    private static final String CLNAME_EVBUS = "net/minecraftforge/fml/common/eventhandler/EventBus";
    private static final String MNAME_EVBUS_POST = "post";
    private static final String MDESC_EVBUS_POST = "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z";

    private static final String CLNAME_MCF = "net/minecraftforge/common/MinecraftForge";
    private static final String FNAME_MCF_EVBUS = "EVENT_BUS";
    private static final String FDESC_MCF_EVBUS = "L" + CLNAME_EVBUS + ";";

    private static final String CLNAME_SCE = "xyz/phanta/aquinasmc/event/SlotClickEvent";
    private static final String MDESC_SCE_INIT = String.format("(L%s;L%s;L%s;II)V", CLNAME_EPLAYER, CLNAME_CONT, CLNAME_CLICK);
    private static final String MNAME_SCE_GETRES = "getResultStack";
    private static final String MDESC_SCE_GETRES = "()Lnet/minecraft/item/ItemStack;";

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
        writer.visitFieldInsn(Opcodes.GETSTATIC, CLNAME_MCF, FNAME_MCF_EVBUS, FDESC_MCF_EVBUS);
        writer.visitTypeInsn(Opcodes.NEW, CLNAME_SCE);
        writer.visitInsn(Opcodes.DUP);
        writer.visitVarInsn(Opcodes.ALOAD, 4);
        writer.visitVarInsn(Opcodes.ALOAD, 0);
        writer.visitVarInsn(Opcodes.ALOAD, 3);
        writer.visitVarInsn(Opcodes.ILOAD, 1);
        writer.visitVarInsn(Opcodes.ILOAD, 2);
        writer.visitMethodInsn(Opcodes.INVOKESPECIAL, CLNAME_SCE, "<init>", MDESC_SCE_INIT, false);
        writer.visitInsn(Opcodes.DUP_X1);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLNAME_EVBUS, MNAME_EVBUS_POST, MDESC_EVBUS_POST, false);
        Label notCancelled = new Label();
        writer.visitJumpInsn(Opcodes.IFEQ, notCancelled);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLNAME_SCE, MNAME_SCE_GETRES, MDESC_SCE_GETRES, false);
        writer.visitInsn(Opcodes.ARETURN);
        writer.visitLabel(notCancelled);
        writer.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { CLNAME_SCE });
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
