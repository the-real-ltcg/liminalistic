package com.ltcg.liminalistic.lore;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

public final class LoreBooks {
	public record Entry(String id, String title, List<String> pages) {
	}

	public static final List<Entry> ENTRIES = List.of(
			new Entry("first_door", "a shaking hand",
					List.of(
							"I found a door where\nno door should be.\nI should not have\nopened it.",
							"The halls go on\nlonger than the\nroom that holds\nthem.",
							"If you are reading\nthis, you followed\nme. Turn back\nwhile you still\nremember your\nname."
					)),
			new Entry("second_door", "unsigned",
					List.of(
							"It remembers the\nshape of my house.\nIt got the doorframe\nwrong. Just\nslightly wrong.",
							"I have stopped\ncounting the rooms.\nThe counting was\nthe first thing\nit took from me."
					)),
			new Entry("third_door", "a name, scratched out",
					List.of(
							"There is a place\nwhere it keeps what\nit takes. I have\nnot reached it.\nI do not think I\nwant to.",
							"If it grows quiet,\nrun. The quiet\nmeans it is close\nenough to stop\npretending to be\nfar away."
					)));

	private LoreBooks() {
	}

	public static Entry byId(String id) {
		return ENTRIES.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(ENTRIES.get(0));
	}

	public static ItemStack createBookStack(Entry entry) {
		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		List<Filterable<Component>> pages = entry.pages().stream()
				.map(page -> Filterable.<Component>passThrough(Component.literal(page)))
				.toList();
		stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
				Filterable.passThrough(entry.title()), "???", 0, pages, true));
		return stack;
	}
}
