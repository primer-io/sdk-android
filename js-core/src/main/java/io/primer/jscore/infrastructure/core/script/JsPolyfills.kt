package io.primer.jscore.infrastructure.core.script

internal object JsPolyfills {

    val TEXT_ENCODER_DECODER = """
        globalThis.TextDecoder = class {
            constructor() {}
            decode(input) {
                const arr = input instanceof Uint8Array ? input : new Uint8Array(input);
                let str = "";
                for (let i = 0; i < arr.length; i++) {
                    str += String.fromCharCode(arr[i]);
                }
                return str;
            }
        };

        globalThis.TextEncoder = class {
            constructor() {}
            encode(str) {
                const bytes = [];
                for (let i = 0; i < str.length; i++) {
                    const code = str.charCodeAt(i);
                    if (code < 0x80) bytes.push(code);
                    else if (code < 0x800) {
                        bytes.push(0xc0 | (code >> 6), 0x80 | (code & 0x3f));
                    } else {
                        bytes.push(
                            0xe0 | (code >> 12),
                            0x80 | ((code >> 6) & 0x3f),
                            0x80 | (code & 0x3f)
                        );
                    }
                }
                return new Uint8Array(bytes);
            }
        };
    """.trimIndent()
}
