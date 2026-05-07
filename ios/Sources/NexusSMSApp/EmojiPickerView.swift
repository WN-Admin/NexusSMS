import SwiftUI

struct EmojiPickerView: View {
    @Environment(\.dismiss) private var dismiss
    let onEmojiSelected: (String) -> Void

    @State private var selectedCategory: EmojiCategory = .smileys
    @State private var recentEmojis: [String] = []

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 8), count: 6)

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                categoryScroll
                Divider()
                emojiGrid
            }
            .navigationTitle("Choose Emoji")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    private var categoryScroll: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(EmojiCategory.allCases, id: \.self) { category in
                    Button {
                        selectedCategory = category
                    } label: {
                        VStack(spacing: 4) {
                            Text(category.icon)
                                .font(.title2)
                            Text(category.label)
                                .font(.caption2)
                                .foregroundColor(selectedCategory == category ? .primary : .secondary)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 6)
                        .background(
                            selectedCategory == category
                                ? Color.accentColor.opacity(0.12)
                                : Color.clear
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
        }
    }

    private var emojiGrid: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 8) {
                if !recentEmojis.isEmpty {
                    sectionHeader("Recent")
                    ForEach(recentEmojis, id: \.self) { emoji in
                        emojiButton(emoji)
                    }
                }
                sectionHeader(selectedCategory.label)
                ForEach(selectedCategory.emojis, id: \.self) { emoji in
                    emojiButton(emoji)
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 8)
        }
    }

    private func sectionHeader(_ title: String) -> some View {
        Text(title)
            .font(.caption)
            .fontWeight(.semibold)
            .foregroundColor(.secondary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, 4)
            .padding(.top, 8)
            .gridCellColumns(6)
    }

    private func emojiButton(_ emoji: String) -> some View {
        Button {
            addRecent(emoji)
            onEmojiSelected(emoji)
            dismiss()
        } label: {
            Text(emoji)
                .font(.largeTitle)
                .frame(minWidth: 44, minHeight: 44)
        }
        .buttonStyle(.plain)
    }

    private func addRecent(_ emoji: String) {
        recentEmojis.removeAll { $0 == emoji }
        recentEmojis.insert(emoji, at: 0)
        if recentEmojis.count > 20 {
            recentEmojis = Array(recentEmojis.prefix(20))
        }
    }
}

enum EmojiCategory: String, CaseIterable {
    case smileys, people, animals, food, travel, activities, objects, symbols

    var icon: String {
        switch self {
        case .smileys:    return "😀"
        case .people:     return "🧑"
        case .animals:    return "🐻"
        case .food:       return "🍕"
        case .travel:     return "✈️"
        case .activities: return "⚽"
        case .objects:    return "💡"
        case .symbols:    return "❤️"
        }
    }

    var label: String {
        switch self {
        case .smileys:    return "Smileys"
        case .people:     return "People"
        case .animals:    return "Animals"
        case .food:       return "Food"
        case .travel:     return "Travel"
        case .activities: return "Activities"
        case .objects:    return "Objects"
        case .symbols:    return "Symbols"
        }
    }

    var emojis: [String] {
        switch self {
        case .smileys:
            return ["😀","😃","😄","😁","😅","😂","🤣","😊","😇","🙂","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🫢","🫣","🤫","🤔","🫡","🤐","🤨","😐","😑","😶","🫥","😏","😒","🙄","😬","😮","😲","😳","🥺","😦","😧","😨","😰","😥","😢","😭","😱","😖","😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬","😈","👿","💀","☠️","💩","🤡","👹","👺","👻","👽","👾","🤖","😺","😸","😹","😻","😼","😽","🙀","😿","😾"]
        case .people:
            return ["👋","🤚","🖐","✋","🖖","🫱","🫲","🫳","🫴","👌","🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝️","🫵","👍","👎","✊","👊","🤛","🤜","👏","🙌","🫶","👐","🤲","🤝","🙏","✍️","💅","🤳","💪","🦾","🦵","🦿","🦶","👣","👂","🦻","👃","🧠","🫀","🫁","🦷","🦴","👀","👁","👅","👄","👶","🧒","👦","👧","🧑","👱","👨","🧔","👩","🧓","👴","👵","🙍","🙎","🙅","🙆","💁","🙋","🧏","🙇","🤦","🤷","👮","🕵️","💂","🥷","👷","🫅","🤴","👸","👳","👲","🧕","🤵","👰","🤰","🫃","🫄","🤱","👼","🎅","🤶","🦸","🦹","🧙","🧚","🧛","🧜","🧝","🧞","🧟","🧌","💆","💇","🚶","🧍","🧎","🏃","💃","🕺","🕴","👯","🧖","🧗","🤸","⛹️","🏋️","🚴","🚵","🤼","🤽","🤾","🤺","⛷️","🏂","🏄","🚣","🏊","🤿","🧘","🛀","🛌","👭","👫","👬","💏","💑","👪","🧑‍🧑‍🧒","🧑‍🧒","👨‍👩‍👧‍👦"]
        case .animals:
            return ["🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈","🙉","🙊","🐒","🐔","🐧","🐦","🐤","🐣","🐥","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🪱","🐛","🦋","🐌","🐞","🐜","🪰","🪲","🪳","🦟","🦗","🕷","🦂","🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦐","🦞","🦀","🐡","🐠","🐟","🐬","🐳","🐋","🦈","🪸","🐊","🐅","🐆","🦓","🦍","🦧","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🦬","🐃","🐂","🐄","🐎","🐖","🐏","🐑","🦙","🐐","🦌","🐕","🐩","🦮","🐕‍🦺","🐈","🐈‍⬛","🪶","🐓","🦃","🦤","🦚","🦜","🦢","🦩","🕊","🐇","🦝","🦨","🦡","🦫","🦦","🦥","🐁","🐀","🐿","🦔","🐾","🐉","🐲"]
        case .food:
            return ["🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥝","🍅","🫒","🥥","🥑","🍆","🥔","🥕","🌽","🌶","🫑","🥒","🥬","🥦","🧄","🧅","🍄","🥜","🫘","🌰","🍞","🥐","🥖","🫓","🥨","🥯","🥞","🧇","🧀","🍖","🍗","🥩","🥓","🍔","🍟","🍕","🌭","🥪","🌮","🌯","🫔","🥙","🧆","🥚","🍳","🥘","🍲","🫕","🥣","🥗","🍿","🧈","🥫","🍝","🍜","🍲","🍛","🍣","🍱","🥟","🦪","🍤","🍙","🍚","🍘","🍥","🥠","🥮","🍢","🍡","🍧","🍨","🍦","🥧","🧁","🍰","🎂","🍮","🍭","🍬","🍫","🍿","🍩","🍪","🌰","🥜","🍯","🥛","🍼","🫖","☕️","🍵","🧃","🥤","🧋","🍶","🍺","🍻","🥂","🍷","🫗","🥃","🍸","🍹","🧉","🍾","🧊","🥄","🍴","🥄","🔪","🫙"]
        case .travel:
            return ["🌍","🌎","🌏","🌐","🗺","🗾","🧭","🏔","⛰","🌋","🗻","🏕","🏖","🏜","🏝","🏞","🏟","🏛","🏗","🧱","🪨","🪵","🛖","🏘","🏚","🏠","🏡","🏢","🏣","🏤","🏥","🏦","🏨","🏩","🏪","🏫","🏬","🏭","🏯","🏰","💒","🗼","🗽","⛪️","🕌","🛕","🕍","⛩","🕋","⛲","⛺️","🌁","🌃","🏙","🌄","🌅","🌆","🌇","🌉","🗿","🛤","🛣","🛸","🚁","🛟","⛵️","🛳","🚤","🛶","✈️","🛩","🛫","🛬","🪂","💺","🚂","🚃","🚄","🚅","🚆","🚇","🚈","🚉","🚊","🚝","🚞","🚋","🚌","🚍","🚎","🚐","🚑","🚒","🚓","🚔","🚕","🚖","🚗","🚘","🚙","🛻","🚚","🚛","🚜","🏎","🏍","🛵","🛺","🛴","🚲","🛹","🛼","🚏","🛑","🚧","⛽️","🛞","🚨","🚥","🚦","🛰","🚀","🛸","🌠"]
        case .activities:
            return ["⚽️","🏀","🏈","⚾️","🥎","🎾","🏐","🏉","🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍","🏏","🪃","🥅","⛳️","🪁","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷","⛸","🥌","🎿","⛷","🏂","🪂","🏋️","🤼","🤸","🤺","⛹️","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚣","🧗","🚵","🚴","🎪","🎭","🎨","🎬","🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🪗","🎸","🪕","🎻","🎲","♟️","🎯","🎳","🎮","🕹️","🎰","🧩"]
        case .objects:
            return ["👓","🕶","🥽","🥼","🦺","👔","👕","👖","🧣","🧤","🧥","🧦","👗","👘","🥻","🩱","🩲","🩳","👙","👚","👛","👜","👝","🎒","🩴","👞","👟","🥾","🥿","👠","👡","👢","👑","👒","🎩","🎓","🧢","🪖","⛑","💄","💍","💎","🔇","🔈","🔉","🔊","📢","📣","📯","🔔","🔕","🎼","🎵","🎶","💹","📱","📲","💻","⌨️","🖥","🖨","🖱","🖲","🕹","🗜","💽","💾","💿","📀","📼","📷","📸","📹","🎥","📽","🎞","📞","☎️","📟","📠","📺","📻","🎙","🎚","🎛","🧭","⏱","⏲","⏰","🕰","⌚️","📻","📡","🔋","🪫","🔌","💡","🔦","🕯","🪔","🧯","🗑","🛢","💰","🪙","💵","💴","💶","💷","🪪","💳","🧾","✉️","📧","📨","📩","📤","📥","📦","📪","📫","📬","📭","📮","📝","📄","📃","📑","🧾","✂️","📌","📍","📎","🖇","📏","📐","🔐","🔒","🔓","🔏","🛡","🔑","🗝","🔨","🪓","⛏","🪚","🔧","🪛","🔩","⚙️","⛓","🪝","🧰","🧲","🪜","🔗","⛓️💥","🧪","🧫","🧬","🔬","🔭","📡","💉","🩸","💊","🩹","🩺","🚪","🛗","🪞","🪟","🛏","🛋","🪑","🛁","🪥","🪣","🧹","🧺","🧻","🚽","🚿","🧴","🧷","🧸","🪅","🪆","🪀","🃏","🀄️","🎴","🔮","🪄","🧿","🪬","🎗","🎟","🎫","🎖","🏆","🏅","🥇","🥈","🥉","⚽️","🏀","🏈","⚾️","🎾","🏐","🏉","🎱","🏓","🏸","🥅","🏒","🏑","🏏","🪃","⛳️","🥊","🥋","🎣","🤿","🥌"]
        case .symbols:
            return ["❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗","💖","💘","💝","💟","☮️","✝️","☪️","🕉","☸️","✡️","🔯","🕎","☯️","☦️","🛐","⛎","♈️","♉️","♊️","♋️","♌️","♍️","♎️","♏️","♐️","♑️","♒️","♓️","🆔","⚛️","🉑","☢️","☣️","📴","📳","🈶","🈚️","🈸","🈺","🈷️","✴️","🆚","🉐","💮","🉐","㊗️","㊙️","🈴","🈵","🈲","🅰️","🅱️","🆎","🆑","🅾️","🆘","🛑","⛔️","📛","🚫","💢","♨️","🚷","🚯","🚳","🚱","🔞","📵","🚭","❗️","❕","❓","❔","‼️","⁉️","🔅","🔆","〽️","⚠️","🚸","🔱","⚜️","🔰","♻️","✅","🈯️","💹","❇️","✳️","❌","⭕️","💠","♾️","💲","💱","🔢","🔡","🔤","🔣","ℹ️","📶","🔀","🔁","🔂","▶️","⏩","⏭","⏯","◀️","⏪","⏮","🔼","⏫","🔽","⏬","⏸","⏹","⏺","⏏️","🎦","🔅","🔆","📶","🔄","🔃","🕐","🕑","🕒","🕓","🕔","🕕","🕖","🕗","🕘","🕙","🕚","🕛","🕜","🕝","🕞","🕟","🕠","🕡","🕢","🕣","🕤","🕥","🕦","🕧","⚪️","🟤","⚫️","🔴","🔵","🟠","🟡","🟢","🟣","🟥","🟧","🟨","🟩","🟦","🟪","🟫","⬛️","⬜️","🔘","🟤","🔲","🔳","🏁","🚩","🎌","🏴","🏳️","🏳️‍🌈","🏴‍☠️"]
        }
    }
}
