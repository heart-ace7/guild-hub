package io.hub.guild.controller.article;

import com.google.common.collect.ImmutableMap;
import io.hub.guild.consts.ArticleCategory;
import io.hub.guild.model.entity.article.Article;
import io.hub.guild.model.form.article.ArticleForm;
import io.hub.guild.model.view.article.ArticleIndexDto;
import io.hub.guild.model.view.article.ArticleIndexDto.ArticleDto;
import io.hub.guild.model.view.article.ArticleShowDto;
import io.hub.guild.service.article.ArticleService;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * ギルドの記事一覧を表示します.
     *
     * @param guildId
     * @return
     */
    @GetMapping("guilds/{guildId}/articles")
    public ModelAndView index(@PathVariable final Long guildId) {
        final List<Article> articles = articleService.fetchArticle(guildId);

        final PegDownProcessor processor = new PegDownProcessor(); // not thread-safe
        final List<ArticleDto> articleDto = articles.stream()
                .map(entity -> {
                    final ArticleDto dto = new ArticleDto();
                    dto.setTitle(entity.getTitle());
                    dto.setHtmlContent(processor.markdownToHtml(entity.getContent()));
                    return dto;
                })
                .collect(Collectors.toList());

        final ArticleIndexDto viewDto = new ArticleIndexDto();
        viewDto.setGuildName("FFL");
        viewDto.setArticles(articleDto);

        return new ModelAndView("article/index", "model", viewDto);
    }

    /**
     * ギルド記事の詳細を表示します.
     *
     * @param guildId
     * @param articleId
     * @return
     */
    @GetMapping("guilds/{guildId}/articles/{articleId}")
    public ModelAndView show(@PathVariable final Long guildId, @PathVariable final Long articleId) {
        final Article article = articleService.fetchArticle(guildId, articleId);

        final PegDownProcessor processor = new PegDownProcessor(Extensions.ALL); // not thread-safe
        final ArticleShowDto.ArticleDto dto = new ArticleShowDto.ArticleDto();
        dto.setTitle(article.getTitle());
        dto.setHtmlContent(processor.markdownToHtml(article.getContent()));

        final ArticleShowDto viewDto = new ArticleShowDto();
        viewDto.setGuildId(guildId);
        viewDto.setGuildName("FFL");
        viewDto.setArticle(dto);

        return new ModelAndView("article/show", "model", viewDto);
    }

    /**
     * ギルド記事の入力画面を表示します.
     *
     * @param guildId
     * @param articleForm
     * @return
     */
    @GetMapping("guilds/{guildId}/articles/input")
    public ModelAndView input(@PathVariable final Long guildId, final ArticleForm articleForm) {
        return renderInput(guildId);
    }

    /**
     * ギルド記事を登録します.
     *
     * @param guildId
     * @param articleForm
     * @param bindingResult
     * @return
     */
    @PostMapping("guilds/{guildId}/articles/create")
    public ModelAndView create(@PathVariable final Long guildId, @Valid final ArticleForm articleForm, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return renderInput(guildId);
        }
        articleService.createArticle(articleForm.getTitle(), articleForm.getContent());

        return new ModelAndView("redirect:/guilds/" + guildId + "/articles");
    }

    /**
     * ギルド記事の編集画面を表示します.
     *
     * @param guildId
     * @param articleId
     * @param articleForm
     * @return
     */
    @GetMapping("guilds/{guildId}/articles/{articleId}/edit")
    public ModelAndView edit(@PathVariable final Long guildId, @PathVariable final Long articleId, final ArticleForm articleForm) {
        final Article article = articleService.fetchArticle(guildId, articleId);

        articleForm.setTitle(article.getTitle());
        articleForm.setContent(article.getContent());

        return renderEdit(guildId);
    }

    /**
     * ギルド記事を更新します.
     *
     * @param guildId
     * @param articleId
     * @param articleForm
     * @param bindingResult
     * @return
     */
    @PutMapping("guilds/{guildId}/articles/{articleId}/update")
    public ModelAndView update(@PathVariable final Long guildId, @PathVariable final Long articleId, @Valid final ArticleForm articleForm, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return renderEdit(guildId);
        }
        articleService.updateArticle(articleId, articleForm.getTitle(), articleForm.getContent());

        return new ModelAndView("redirect:/guilds/" + guildId + "/articles");
    }

    private ModelAndView renderInput(final Long guildId) {
        final Map<String, Object> params = ImmutableMap.of(
                "guildName", "FFL",
                "categories", ArticleCategory.values()
        );
        return new ModelAndView("article/input", params);
    }

    private ModelAndView renderEdit(final Long guildId) {
        final Map<String, Object> params = ImmutableMap.of(
                "guildName", "FFL",
                "categories", ArticleCategory.values()
        );
        return new ModelAndView("article/edit", params);
    }
}