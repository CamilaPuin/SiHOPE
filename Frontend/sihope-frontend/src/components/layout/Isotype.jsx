export default function Isotype({ negative = false, className = "" }) {
    const classes = ["iso", negative ? "iso--neg" : "", className]
        .filter(Boolean)
        .join(" ");

    return (
        <svg
            className={classes}
            viewBox="0 0 200 180"
            role="img"
            aria-label="Isotipo SiHope"
        >
            <g className="iso-line" strokeWidth="8" strokeLinecap="round">
                <line x1="45" y1="90" x2="140" y2="45" />
                <line x1="45" y1="90" x2="160" y2="90" />
                <line x1="45" y1="90" x2="140" y2="135" />
            </g>
            <circle className="iso-node" cx="45" cy="90" r="22" />
            <circle className="iso-ring" cx="140" cy="45" r="26" />
            <polygon className="iso-icon" points="140,34 158,42 140,50 122,42" />
            <path
                className="iso-icon"
                d="M129 46 L129 53 Q140 59 151 53 L151 46"
                fill="none"
                stroke="#111111"
                strokeWidth="3"
            />
            <line
                className="iso-icon"
                x1="158"
                y1="42"
                x2="158"
                y2="54"
                stroke="#111111"
                strokeWidth="2.5"
            />
            <circle className="iso-ring" cx="160" cy="90" r="26" />
            <path
                className="iso-icon"
                d="M160 80 C154 78 148 79 145 81 L145 100 C148 98 154 97 160 99 Z"
            />
            <path
                className="iso-icon"
                d="M160 80 C166 78 172 79 175 81 L175 100 C172 98 166 97 160 99 Z"
            />
            <circle className="iso-ring" cx="140" cy="135" r="26" />
            <circle className="iso-icon" cx="140" cy="128" r="6.5" />
            <path className="iso-icon" d="M128 149 Q140 134 152 149 Z" />
        </svg>
    );
}

export function Wordmark({ negative = false }) {
    return (
        <span className={`wordmark ${negative ? "wordmark--neg" : ""}`}>
            <span className="si">Si</span>
            <span className="hope">Hope</span>
        </span>
    );
}
